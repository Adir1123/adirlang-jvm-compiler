package lexer;

public class Token {
    public final TokenType type;
    public final String lexeme;     // original text (e.g. "let", "x", "123")
    public final Integer intValue;  // only for NUMBER, else null
    public final int line;          // 1-based
    public final int col;           // 1-based (start column)

    public Token(TokenType type, String lexeme, Integer intValue, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.intValue = intValue;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        if (type == TokenType.NUMBER) {
            return type + "(" + intValue + ") @" + line + ":" + col;
        }
        if (type == TokenType.IDENT) {
            return type + "(" + lexeme + ") @" + line + ":" + col;
        }
        return type + " @" + line + ":" + col;
    }
}
