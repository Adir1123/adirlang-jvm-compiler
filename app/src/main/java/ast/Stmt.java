package ast;

/**
 * Sealed base for all statement nodes in the AST.
 *
 * <p>Sealing the interface documents the complete set of statement kinds and
 * prevents unknown subtypes from being introduced externally, making every
 * pass that dispatches on {@code Stmt} easier to reason about.
 */
public sealed interface Stmt permits LetStmt, PrintStmt, IfStmt {}
