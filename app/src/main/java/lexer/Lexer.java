package lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes AdirLang source text into a flat {@link Token} list.
 *
 * <p>Line and column numbers are 1-based and attached to every token for
 * use in diagnostic messages later in the pipeline.
 */
public class Lexer {

    private final String input;
    private int pos  = 0;
    private int line = 1;
    private int col  = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            skipWhitespace();

            int startLine = line;
            int startCol  = col;
            char c = peek();

            if (c == '\0') {
                tokens.add(new Token(TokenType.EOF, "", null, startLine, startCol));
                return tokens;
            }

            // Single-character symbols — map char to TokenType, then create token uniformly
            TokenType symbolType = switch (c) {
                case '+' -> TokenType.PLUS;
                case '*' -> TokenType.STAR;
                case '=' -> TokenType.EQUALS;
                case ';' -> TokenType.SEMI;
                case '(' -> TokenType.LPAREN;
                case ')' -> TokenType.RPAREN;
                case '{' -> TokenType.LBRACE;
                case '}' -> TokenType.RBRACE;
                default  -> null;
            };

            if (symbolType != null) {
                advance();
                tokens.add(new Token(symbolType, String.valueOf(c), null, startLine, startCol));
                continue;
            }

            // Number literal
            if (isDigit(c)) {
                tokens.add(scanNumber(startLine, startCol));
                continue;
            }

            // Identifier or keyword
            if (isLetter(c) || c == '_') {
                tokens.add(scanIdentOrKeyword(startLine, startCol));
                continue;
            }

            throw errorAt(startLine, startCol, "Unexpected character: '" + c + "'");
        }
    }

    // ------------------------------------------------------------------ scan

    private Token scanNumber(int startLine, int startCol) {
        int start = pos;
        while (isDigit(peek())) advance();
        String text = input.substring(start, pos);
        try {
            return new Token(TokenType.NUMBER, text, Integer.parseInt(text), startLine, startCol);
        } catch (NumberFormatException e) {
            throw errorAt(startLine, startCol, "Integer literal out of range: " + text);
        }
    }

    private Token scanIdentOrKeyword(int startLine, int startCol) {
        int start = pos;
        while (isIdentChar(peek())) advance();
        String text = input.substring(start, pos);
        TokenType type = switch (text) {
            case "let"   -> TokenType.LET;
            case "print" -> TokenType.PRINT;
            case "if"    -> TokenType.IF;
            case "else"  -> TokenType.ELSE;
            default      -> TokenType.IDENT;
        };
        return new Token(type, text, null, startLine, startCol);
    }

    // ---------------------------------------------------------------- helpers

    private void skipWhitespace() {
        while (isWhitespace(peek())) advance();
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    /** Consumes the current character, updating line/col tracking. */
    private char advance() {
        char c = peek();
        if (c == '\0') return '\0';
        pos++;
        if (c == '\n') { line++; col = 1; }
        else           { col++; }
        return c;
    }

    private boolean isWhitespace(char c) { return c == ' ' || c == '\t' || c == '\r' || c == '\n'; }
    private boolean isDigit(char c)      { return c >= '0' && c <= '9'; }
    private boolean isLetter(char c)     { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private boolean isIdentChar(char c)  { return isLetter(c) || isDigit(c) || c == '_'; }

    private RuntimeException errorAt(int ln, int cl, String msg) {
        return new RuntimeException("Lex error at " + ln + ":" + cl + " — " + msg);
    }
}
