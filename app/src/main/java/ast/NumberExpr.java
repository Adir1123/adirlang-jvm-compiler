package ast;

public class NumberExpr implements Expr {
    public final int value;
    public final int line;
    public final int col;

    public NumberExpr(int value, int line, int col) {
        this.value = value;
        this.line = line;
        this.col = col;
    }
}
