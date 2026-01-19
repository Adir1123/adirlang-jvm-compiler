package ast;

public class BinaryExpr implements Expr {
    public final Expr left;
    public final Expr right;
    public final char op;
    public final int line;
    public final int col;

    public BinaryExpr(Expr left, char op, Expr right, int line, int col) {
        this.left = left;
        this.op = op;
        this.right = right;
        this.line = line;
        this.col = col;
    }
}
