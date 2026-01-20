package lexer;

public enum TokenType {
    // Keywords
    LET,
    PRINT,

    // Identifiers & literals
    IDENT,
    NUMBER,

    // Symbols
    PLUS,       // +
    STAR,       // *
    EQUALS,     // =
    SEMI,       // ;
    LPAREN,     // (
    RPAREN,     // )
    IF,
    LBRACE,
    RBRACE,
    ELSE,

    EOF
}
