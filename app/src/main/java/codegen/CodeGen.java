package codegen;

import ast.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * JVM bytecode emitter for AdirLang.
 *
 * <p>Translates a list of {@link Stmt} AST nodes into ASM method-visitor
 * calls. Each instance is bound to a single {@link MethodVisitor} and a
 * {@link CodeGenContext} that manages local-variable slot allocation.
 */
public class CodeGen {

    private final MethodVisitor mv;
    private final CodeGenContext ctx;

    public CodeGen(MethodVisitor mv) {
        this.mv  = mv;
        this.ctx = new CodeGenContext();
    }

    // ---------------------------------------------------------------- Statements

    public void emitStmt(Stmt stmt) {
        if (stmt instanceof LetStmt s) {
            emitExpr(s.value());
            mv.visitVarInsn(ISTORE, ctx.allocate(s.name()));
            return;
        }
        if (stmt instanceof PrintStmt s) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            emitExpr(s.expr());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            return;
        }
        if (stmt instanceof IfStmt s) emitIf(s);
    }

    private void emitIf(IfStmt s) {
        emitExpr(s.condition());

        if (s.elseBranch().isEmpty()) {
            // No else branch: a single label at the end suffices.
            // Skip the GOTO that would be needed if an else block existed.
            Label end = new Label();
            mv.visitJumpInsn(IFEQ, end);
            s.thenBranch().forEach(this::emitStmt);
            mv.visitLabel(end);
        } else {
            // Has else branch: jump over then-block into else-block on false.
            Label elseLabel = new Label();
            Label end       = new Label();
            mv.visitJumpInsn(IFEQ, elseLabel);
            s.thenBranch().forEach(this::emitStmt);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(elseLabel);
            s.elseBranch().get().forEach(this::emitStmt);
            mv.visitLabel(end);
        }
    }

    // ---------------------------------------------------------------- Expressions

    private void emitExpr(Expr expr) {
        if (expr instanceof NumberExpr n) { pushInt(n.value()); return; }
        if (expr instanceof VarExpr v)    { mv.visitVarInsn(ILOAD, ctx.lookup(v.name())); return; }
        if (expr instanceof BinaryExpr b) {
            emitExpr(b.left());
            emitExpr(b.right());
            switch (b.op()) {
                case ADD -> mv.visitInsn(IADD);
                case MUL -> mv.visitInsn(IMUL);
            }
        }
    }

    // ------------------------------------------------------- Integer push helper

    /**
     * Emits the most compact instruction sequence that pushes {@code value}
     * onto the operand stack.
     *
     * <ul>
     *   <li>{@code -1..5}   — {@code ICONST_M1} / {@code ICONST_0..5} (1 byte)</li>
     *   <li>{@code -128..127}  — {@code BIPUSH} (2 bytes)</li>
     *   <li>{@code -32768..32767} — {@code SIPUSH} (3 bytes)</li>
     *   <li>anything else   — {@code LDC} (constant pool entry)</li>
     * </ul>
     */
    private void pushInt(int value) {
        switch (value) {
            case -1 -> mv.visitInsn(ICONST_M1);
            case  0 -> mv.visitInsn(ICONST_0);
            case  1 -> mv.visitInsn(ICONST_1);
            case  2 -> mv.visitInsn(ICONST_2);
            case  3 -> mv.visitInsn(ICONST_3);
            case  4 -> mv.visitInsn(ICONST_4);
            case  5 -> mv.visitInsn(ICONST_5);
            default -> {
                if      (value >= -128   && value <= 127)   mv.visitIntInsn(BIPUSH, value);
                else if (value >= -32768 && value <= 32767) mv.visitIntInsn(SIPUSH, value);
                else                                        mv.visitLdcInsn(value);
            }
        }
    }
}
