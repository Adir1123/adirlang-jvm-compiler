package codegen;

import ast.*;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CodeGen implements Opcodes {

    // ============================
    // Helper — pushes an integer constant onto the operand stack
    // Chooses the most efficient instruction based on value range
    // ============================

    static void pushInt(MethodVisitor mv, int value) {


        if (value == -1) { mv.visitInsn(ICONST_M1); return; }
        if (value == 0)  { mv.visitInsn(ICONST_0);  return; }
        if (value == 1)  { mv.visitInsn(ICONST_1);  return; }
        if (value == 2)  { mv.visitInsn(ICONST_2);  return; }
        if (value == 3)  { mv.visitInsn(ICONST_3);  return; }
        if (value == 4)  { mv.visitInsn(ICONST_4);  return; }
        if (value == 5)  { mv.visitInsn(ICONST_5);  return; }

        if (value >= -128 && value <= 127) {
            mv.visitIntInsn(BIPUSH, value);
            return;
        }

        if (value >= -32768 && value <= 32767) {
            mv.visitIntInsn(SIPUSH, value);
            return;
        }

        mv.visitLdcInsn(value);
    }



    // ============================
    // Codegen — converts Expr nodes into JVM bytecode instructions
    // ============================

    public static void emitExpr(MethodVisitor mv, Expr expr, Map<String, Integer> locals) {

        
        if (expr instanceof NumberExpr n) {
            pushInt(mv, n.value);
            return;
        }

        
        if (expr instanceof VarExpr v) {
            Integer slot = locals.get(v.name);
            if (slot == null) {
                throw new RuntimeException("Codegen error at " + v.line + ":" + v.col + " - Undefined variable: " + v.name);
            }
            mv.visitVarInsn(ILOAD, slot);
            return;
        }

        // Binary: emit left, emit right, then op
        if (expr instanceof BinaryExpr b) {
            emitExpr(mv, b.left, locals);
            emitExpr(mv, b.right, locals);

            if (b.op == '+') {
                mv.visitInsn(IADD);
                return;
            }

            if (b.op == '*') {
                mv.visitInsn(IMUL);
                return;
            }

            throw new RuntimeException("Codegen error at " + b.line + ":" + b.col + " - Unknown operator: " + b.op);
        }

        throw new RuntimeException("Unknown Expr node");
    }


    // ============================
    // Codegen — converts Stmt nodes into JVM bytecode instructions
    // ============================

    public static void emitStmt(MethodVisitor mv, Stmt stmt, Map<String, Integer> locals, int[] nextLocalIndex) {

        // let x = expr;
        if (stmt instanceof LetStmt s) {
            emitExpr(mv, s.value, locals);

            int slot = locals.computeIfAbsent(s.name, k -> nextLocalIndex[0]++);
            mv.visitVarInsn(ISTORE, slot);
            return;
        }

        // print expr;
        if (stmt instanceof PrintStmt s) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"); // System.out
            emitExpr(mv, s.expr, locals);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false); // println
            return;
        }

        throw new RuntimeException("Unknown Stmt node");
    }
}
