import ast.*;
import parser.Parser;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// My compiler to bytecode writen in java
public class BytecodeHello implements Opcodes {


    // ============================
    // Codegen — converts Expr into JVM bytecode instructions to be executed by the JVM, at runtime
    // ============================

    static void emitExpr(MethodVisitor mv, Expr expr, Map<String, Integer> locals) {

        // Number: push int
        if (expr instanceof NumberExpr n) {
            mv.visitIntInsn(BIPUSH, n.value);
            return;
        }

        // Variable: load from local slot
        if (expr instanceof VarExpr v) {
            Integer slot = locals.get(v.name);
            if (slot == null) {
                throw new RuntimeException("Undefined variable: " + v.name);
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

            throw new RuntimeException("Unknown operator: " + b.op);
        }

        throw new RuntimeException("Unknown Expr node");

    }


    // ============================
    // Codegen — converts Stms into JVM bytecode instructions to be executed by the JVM, at runtime
    // ============================

    static void emitStmt(MethodVisitor mv, Stmt stmt, Map<String, Integer> locals, int[] nextLocalIndex) {

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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false); // println(int)
            return;
        }

        throw new RuntimeException("Unknown Stmt node");
    }


    // =========================
    // Main: compile program.adir into Main.class
    // =========================

    public static void main(String[] args) throws Exception {

        // 1. Read source file - My language
        String source = new String(BytecodeHello.class.getResourceAsStream("/program.adir").readAllBytes()).trim();

        Parser parser = new Parser(source);
        List<Stmt> program = parser.parseProgram();
        

        // Main.class
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // public class Main extends Object
        cw.visit(V17, ACC_PUBLIC, "Main", null, "java/lang/Object", null);

        // constructor: public Main() { super(); } =====
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();

        // public static void main(String[] args) 
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        
        // Symbol Table: name -> local slot
        Map<String, Integer> locals = new HashMap<>();
        int[] nextLocalIndex = new int[]{1}; // slot 0 is for args


        
        for (Stmt stmt : program) {
            emitStmt(mv, stmt, locals, nextLocalIndex);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        // Write to Main.class
        Files.write(Path.of("Main.class"), cw.toByteArray());

        System.out.println("Compiled program.adir → Main.class");
        System.out.println("Run with: java Main");
    }
}
