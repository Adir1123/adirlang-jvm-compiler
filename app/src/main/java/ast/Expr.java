package ast;

/**
 * Sealed base for all expression nodes in the AST.
 *
 * <p>Sealing the interface documents the complete set of expression kinds and
 * prevents unknown subtypes from being introduced externally, making every
 * pass that dispatches on {@code Expr} easier to reason about.
 */
public sealed interface Expr permits NumberExpr, VarExpr, BinaryExpr {}
