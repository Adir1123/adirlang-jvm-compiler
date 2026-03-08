package ast;

/**
 * A binary expression, e.g. {@code a + b} or {@code a * b}.
 *
 * <p>The operator is an {@link Op} enum value rather than a raw {@code char}
 * so that every call site is forced to handle all operator kinds explicitly.
 */
public record BinaryExpr(Expr left, Op op, Expr right, int line, int col) implements Expr {}
