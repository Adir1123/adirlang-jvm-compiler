package ast;

/**
 * Binary operators supported by the language.
 * Using an enum instead of a raw {@code char} ensures every operator is
 * explicitly handled at each call site and removes the runtime "unknown
 * operator" fallback in code-generation.
 */
public enum Op {
    ADD,
    MUL
}
