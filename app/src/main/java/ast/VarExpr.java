package ast;

/** A variable reference expression, e.g. {@code x}. */
public record VarExpr(String name, int line, int col) implements Expr {}
