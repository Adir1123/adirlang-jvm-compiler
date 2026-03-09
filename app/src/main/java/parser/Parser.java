package parser;

import ast.*;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Recursive-descent parser for AdirLang.
 *
 * <p>Accepts a pre-tokenized {@link List}&lt;{@link Token}&gt; so the lexing
 * and parsing stages remain independently testable.
 *
 * <p>Operator precedence (low → high): {@code +} → {@code *} → primary.
 */
public class Parser {

    private final List<Token> tokens;
    private int index = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parseProgram() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        return stmts;
    }

    // ------------------------------------------------------------ Statements

    private Stmt parseStatement() {
        if (match(TokenType.LET))   return parseLetStmt();
        if (match(TokenType.PRINT)) return parsePrintStmt();
        if (match(TokenType.IF))    return parseIfStmt();

        Token t = peek();
        throw errorAt(t, "Expected a statement (let, print, if)");
    }

    private LetStmt parseLetStmt() {
        Token letTok  = previous();
        Token nameTok = consume(TokenType.IDENT,  "Expected identifier after 'let'");
        consume(TokenType.EQUALS, "Expected '=' after identifier");
        Expr value = parseAddition();
        consume(TokenType.SEMI, "Expected ';' after expression");
        return new LetStmt(nameTok.lexeme(), value, letTok.line(), letTok.col());
    }

    private PrintStmt parsePrintStmt() {
        Token printTok = previous();
        Expr expr = parseAddition();
        consume(TokenType.SEMI, "Expected ';' after expression");
        return new PrintStmt(expr, printTok.line(), printTok.col());
    }

    private IfStmt parseIfStmt() {
        Token ifTok = previous();
        consume(TokenType.LPAREN, "Expected '(' after 'if'");
        Expr condition = parseAddition();
        consume(TokenType.RPAREN, "Expected ')' after condition");

        List<Stmt> thenBranch = parseBlock();

        Optional<List<Stmt>> elseBranch = match(TokenType.ELSE)
                ? Optional.of(parseBlock())
                : Optional.empty();

        return new IfStmt(condition, thenBranch, elseBranch, ifTok.line(), ifTok.col());
    }

    // ---------------------------------------------------------------- Blocks

    private List<Stmt> parseBlock() {
        consume(TokenType.LBRACE, "Expected '{'");
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        consume(TokenType.RBRACE, "Expected '}'");
        return stmts;
    }

    // ---------------------------------------------------------- Expressions
    // Precedence (low → high): addition → multiplication → primary

    private Expr parseAddition()       { return parseBinaryOp(this::parseMultiplication, TokenType.PLUS, Op.ADD); }
    private Expr parseMultiplication() { return parseBinaryOp(this::parsePrimary,        TokenType.STAR, Op.MUL); }

    private Expr parseBinaryOp(Supplier<Expr> nextLevel, TokenType token, Op op) {
        Expr left = nextLevel.get();
        while (match(token)) {
            Token opTok = previous();
            left = new BinaryExpr(left, op, nextLevel.get(), opTok.line(), opTok.col());
        }
        return left;
    }

    private Expr parsePrimary() {
        if (match(TokenType.LPAREN)) {
            Expr inside = parseAddition();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return inside;
        }
        if (match(TokenType.NUMBER)) {
            Token n = previous();
            return new NumberExpr(n.intValue(), n.line(), n.col());
        }
        if (match(TokenType.IDENT)) {
            Token name = previous();
            return new VarExpr(name.lexeme(), name.line(), name.col());
        }
        throw errorAt(peek(), "Expected a primary expression (number, variable, or parenthesized expression)");
    }

    // --------------------------------------------------------- Token helpers

    private Token peek()     { return tokens.get(index); }
    private Token previous() { return tokens.get(index - 1); }

    private boolean check(TokenType type) { return peek().type() == type; }

    private boolean match(TokenType type) {
        if (!check(type)) return false;
        index++;
        return true;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return tokens.get(index++);
        throw errorAt(peek(), message);
    }

    private RuntimeException errorAt(Token t, String msg) {
        return new RuntimeException("Parse error at " + t.line() + ":" + t.col() + " — " + msg);
    }
}
