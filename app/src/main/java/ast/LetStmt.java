package ast;

public class LetStmt implements Stmt {
    public final String name;
    public final Expr value;
    public final int line;
    public final int col;

    public LetStmt(String name, Expr value, int line, int col) {
        this.name = name;
        this.value = value;
        this.line = line;
        this.col = col;
    }
}
