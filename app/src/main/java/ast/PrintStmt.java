package ast;

public class PrintStmt implements Stmt {
    public final Expr expr;
    public final int line;
    public final int col;

    public PrintStmt(Expr expr, int line, int col) {
        this.expr = expr;
        this.line = line;
        this.col = col;
    }
}
