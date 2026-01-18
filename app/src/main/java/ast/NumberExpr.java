package ast;

public class NumberExpr implements Expr {
    public final int value;

    public NumberExpr(int value) {
        this.value = value;
    }
}
