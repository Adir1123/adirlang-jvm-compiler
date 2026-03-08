package semantic;

import ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Semantic analysis pass over the AST.
 *
 * <p>Validates:
 * <ul>
 *   <li>All variables are declared before use.</li>
 *   <li>No variable is declared twice in the same scope.</li>
 * </ul>
 *
 * <p><strong>Scoping rules:</strong> variables declared inside an {@code if}
 * or {@code else} block are local to that block and do not leak into sibling
 * branches or the enclosing scope.  Outer-scope variables are visible inside
 * inner blocks (read-only from the inner block's perspective).
 */
public class SemanticAnalyzer {

    public void check(List<Stmt> program) {
        Set<String> defined = new HashSet<>();
        checkBlock(program, defined);
    }

    // ------------------------------------------------------- statement checks

    private void checkBlock(List<Stmt> stmts, Set<String> defined) {
        for (Stmt stmt : stmts) {
            checkStmt(stmt, defined);
        }
    }

    private void checkStmt(Stmt stmt, Set<String> defined) {
        if (stmt instanceof LetStmt s) {
            checkExpr(s.value(), defined);
            if (defined.contains(s.name())) {
                error(s.line(), s.col(), "Variable '" + s.name() + "' is already declared in this scope");
            }
            defined.add(s.name());
            return;
        }

        if (stmt instanceof PrintStmt s) {
            checkExpr(s.expr(), defined);
            return;
        }

        if (stmt instanceof IfStmt s) {
            checkExpr(s.condition(), defined);
            // Each branch receives a copy of the enclosing scope so that
            // declarations inside one branch don't leak into the other or
            // into the enclosing scope.
            checkBlock(s.thenBranch(), new HashSet<>(defined));
            s.elseBranch().ifPresent(branch -> checkBlock(branch, new HashSet<>(defined)));
            return;
        }

        // Unreachable for a well-formed sealed Stmt hierarchy.
        throw new RuntimeException("Unknown Stmt node in semantic analysis: " + stmt.getClass().getSimpleName());
    }

    // -------------------------------------------------------- expression checks

    private void checkExpr(Expr expr, Set<String> defined) {
        if (expr instanceof NumberExpr) {
            return;
        }

        if (expr instanceof VarExpr v) {
            if (!defined.contains(v.name())) {
                error(v.line(), v.col(), "Undefined variable '" + v.name() + "'");
            }
            return;
        }

        if (expr instanceof BinaryExpr b) {
            checkExpr(b.left(), defined);
            checkExpr(b.right(), defined);
            return;
        }

        // Unreachable for a well-formed sealed Expr hierarchy.
        throw new RuntimeException("Unknown Expr node in semantic analysis: " + expr.getClass().getSimpleName());
    }

    // ----------------------------------------------------------------- error

    private void error(int line, int col, String msg) {
        throw new RuntimeException("Semantic error at " + line + ":" + col + " — " + msg);
    }
}
