package ast;

/** An integer literal expression, e.g. {@code 42}. */
public record NumberExpr(int value, int line, int col) implements Expr {}
