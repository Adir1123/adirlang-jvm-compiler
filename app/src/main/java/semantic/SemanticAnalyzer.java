package semantic;

import ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticAnalyzer {

    public void check(List<Stmt> program) {
        Set<String> defined = new HashSet<>();

        for (Stmt s : program) {
            checkStmt(s, defined);
        }
    }

    private void checkStmt(Stmt stmt, Set<String> defined) {
        if (stmt instanceof LetStmt s) {
            checkExpr(s.value, defined);
            defined.add(s.name);
            return;
        }

        if (stmt instanceof PrintStmt s) {
            checkExpr(s.expr, defined);
            return;
        }

        throw new RuntimeException("Unknown Stmt node (semantic)");
    }

    private void checkExpr(Expr expr, Set<String> defined) {
        if (expr instanceof NumberExpr) return;

        if (expr instanceof VarExpr v) {
            if (!defined.contains(v.name)) {
                error(v.line, v.col, "Undefined variable: " + v.name);
            }
            return;
        }

        if (expr instanceof BinaryExpr b) {
            checkExpr(b.left, defined);
            checkExpr(b.right, defined);
            return;
        }

        throw new RuntimeException("Unknown Expr node (semantic)");
    }

    private void error(int line, int col, String msg) {
        throw new RuntimeException("Semantic error at " + line + ":" + col + " - " + msg);
    }
}
