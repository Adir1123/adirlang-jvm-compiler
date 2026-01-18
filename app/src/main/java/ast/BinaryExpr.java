package ast;

public class BinaryExpr implements Expr {
    public final Expr left;
    public final Expr right;
    public final char op;

    public BinaryExpr(Expr left, char op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
