package ast;

public class LetStmt implements Stmt {
    public final String name;
    public final Expr value;

    public LetStmt(String name, Expr value) {
        this.name = name;
        this.value = value;
    }
}
