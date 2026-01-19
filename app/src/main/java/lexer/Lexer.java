package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String input;
    private int i = 0;

    private int line = 1; 
    private int col = 1;  

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            skipWhitespace();

            int startLine = line;
            int startCol = col;

            char c = peek();
            if (c == '\0') {
                tokens.add(new Token(TokenType.EOF, "", null, startLine, startCol));
                return tokens;
            }

            // Single-character symbols
            if (c == '+') { advance(); tokens.add(new Token(TokenType.PLUS, "+", null, startLine, startCol)); continue; }
            if (c == '*') { advance(); tokens.add(new Token(TokenType.STAR, "*", null, startLine, startCol)); continue; }
            if (c == '=') { advance(); tokens.add(new Token(TokenType.EQUALS, "=", null, startLine, startCol)); continue; }
            if (c == ';') { advance(); tokens.add(new Token(TokenType.SEMI, ";", null, startLine, startCol)); continue; }
            if (c == '(') { advance(); tokens.add(new Token(TokenType.LPAREN, "(", null, startLine, startCol)); continue; }
            if (c == ')') { advance(); tokens.add(new Token(TokenType.RPAREN, ")", null, startLine, startCol)); continue; }

            // Number
            if (isDigit(c)) {
                String num = readNumber();
                int value;
                try {
                    value = Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    throw errorAt(startLine, startCol, "Integer literal out of range: " + num);
                }
                tokens.add(new Token(TokenType.NUMBER, num, value, startLine, startCol));
                continue;
            }

            // Identifier / keyword
            if (isLetter(c) || c == '_') {
                String ident = readIdentifier();
                TokenType type = switch (ident) {
                    case "let" -> TokenType.LET;
                    case "print" -> TokenType.PRINT;
                    default -> TokenType.IDENT;
                };
                tokens.add(new Token(type, ident, null, startLine, startCol));
                continue;
            }

            throw errorAt(startLine, startCol, "Unexpected character: '" + c + "'");
        }
    }

    // ---------------- helpers ----------------

    private void skipWhitespace() {
        while (true) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
                continue;
            }
            if (c == '\n') {
                advance(); // advance updates line/col
                continue;
            }
            break;
        }
    }

    private char peek() {
        if (i >= input.length()) return '\0';
        return input.charAt(i);
    }

    private char advance() {
        char c = peek();
        if (c == '\0') return '\0';

        i++;
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private String readNumber() {
        int start = i;
        while (isDigit(peek())) advance();
        return input.substring(start, i);
    }

    private String readIdentifier() {
        int start = i;
        while (true) {
            char c = peek();
            if (isLetter(c) || isDigit(c) || c == '_') {
                advance();
            } else {
                break;
            }
        }
        return input.substring(start, i);
    }

    private RuntimeException errorAt(int ln, int cl, String msg) {
        return new RuntimeException("Lex error at " + ln + ":" + cl + " - " + msg);
    }
}
