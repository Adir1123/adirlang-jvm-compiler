package lexer;

/**
 * A single token produced by the lexer.
 *
 * <p>{@code intValue} is non-null only for {@link TokenType#NUMBER} tokens;
 * it is {@code null} for every other token type.
 */
public record Token(TokenType type, String lexeme, Integer intValue, int line, int col) {

    @Override
    public String toString() {
        return switch (type) {
            case NUMBER -> type + "(" + intValue + ") @" + line + ":" + col;
            case IDENT  -> type + "(" + lexeme  + ") @" + line + ":" + col;
            default     -> type + " @" + line + ":" + col;
        };
    }
}
