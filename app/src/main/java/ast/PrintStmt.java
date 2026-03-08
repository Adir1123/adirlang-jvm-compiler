package ast;

/** A print statement, e.g. {@code print expr;}. */
public record PrintStmt(Expr expr, int line, int col) implements Stmt {}
