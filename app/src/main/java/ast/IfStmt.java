package ast;

import java.util.List;
import java.util.Optional;

/**
 * An if/else statement, e.g. {@code if (cond) { ... } else { ... }}.
 *
 * <p>{@code elseBranch} is {@link Optional#empty()} when no {@code else}
 * keyword was present. Using {@code Optional} instead of a nullable field
 * makes the absent case explicit at every call site.
 */
public record IfStmt(
        Expr condition,
        List<Stmt> thenBranch,
        Optional<List<Stmt>> elseBranch,
        int line,
        int col
) implements Stmt {}
