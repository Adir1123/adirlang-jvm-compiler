package lexer;

public enum TokenType {

    // Keywords
    LET, PRINT, IF, ELSE,

    // Identifiers and literals
    IDENT, NUMBER,

    // Operators
    PLUS,   // +
    STAR,   // *
    EQUALS, // =

    // Delimiters
    SEMI,   // ;
    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }

    EOF
}
