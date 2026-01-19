package ast;

public class VarExpr implements Expr {
    public final String name;
    public final int line;
    public final int col;

    public VarExpr(String name, int line, int col) {
        this.name = name;
        this.line = line;
        this.col = col;
    }
}
