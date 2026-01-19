package parser;

import ast.*;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

// Building AST
public class Parser {

    private final List<Token> tokens;
    private int index = 0;


    public Parser(String source) {
        this.tokens = new Lexer(source).tokenize();
    }

    // public entry point
    public List<Stmt> parseProgram() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        return stmts;
    }

    // ---------------- Statements ----------------

    private Stmt parseStatement() {
        if (match(TokenType.LET)) {
            Token nameTok = consume(TokenType.IDENT, "Expected identifier after 'let'");
            consume(TokenType.EQUALS, "Expected '=' after identifier");
            Expr value = parseAddition();
            consume(TokenType.SEMI, "Expected ';' after expression");
            return new LetStmt(nameTok.lexeme, value);
        }

        if (match(TokenType.PRINT)) {
            Expr expr = parseAddition();
            consume(TokenType.SEMI, "Expected ';' after expression");
            return new PrintStmt(expr);
        }

        Token t = peek();
        throw errorAt(t, "Unknown statement");
    }

    // ---------------- Expressions ----------------
    // precedence:
    // multiplication (*) > addition (+)

    private Expr parseAddition() {
        Expr expr = parseMultiplication();
        while (match(TokenType.PLUS)) {
            Expr right = parseMultiplication();
            expr = new BinaryExpr(expr, '+', right);
        }
        return expr;
    }

    private Expr parseMultiplication() {
        Expr expr = parsePrimary();
        while (match(TokenType.STAR)) {
            Expr right = parsePrimary();
            expr = new BinaryExpr(expr, '*', right);
        }
        return expr;
    }

    private Expr parsePrimary() {
        if (match(TokenType.LPAREN)) {
            Expr inside = parseAddition();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return inside;
        }

        if (match(TokenType.NUMBER)) {
            Token n = previous();
            return new NumberExpr(n.intValue);
        }

        if (match(TokenType.IDENT)) {
            Token name = previous();
            return new VarExpr(name.lexeme);
        }

        Token t = peek();
        throw errorAt(t, "Expected primary expression");
    }

    // ---------------- Token helpers ----------------

    private Token peek() {
        return tokens.get(index);
    }

    private Token previous() {
        return tokens.get(index - 1);
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            index++;
            return true;
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return tokens.get(index++);
        throw errorAt(peek(), message);
    }

    private RuntimeException errorAt(Token t, String msg) {
        return new RuntimeException("Parse error at " + t.line + ":" + t.col + " - " + msg);
    }
}
