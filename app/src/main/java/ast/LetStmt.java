package ast;

/** A variable declaration statement, e.g. {@code let x = expr;}. */
public record LetStmt(String name, Expr value, int line, int col) implements Stmt {}
