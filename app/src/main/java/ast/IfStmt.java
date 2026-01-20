package ast;

import java.util.List;

public class IfStmt implements Stmt {

    public final Expr condition;
    public final List<Stmt> thenBranch;
    public final List<Stmt> elseBranch;
    public final int line;
    public final int col;

    public IfStmt(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch, int line, int col) {

        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        this.line = line;
        this.col = col;
    }
}
