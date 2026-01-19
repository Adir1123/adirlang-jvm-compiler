package adirlanguage;

import ast.*;
import codegen.CodeGen;
import parser.Parser;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// A compiler written in Java that translates a custom language into JVM bytecode (.class files)
public class AdirLangCompiler implements Opcodes {


    // =========================
    // Main: compile program.adir into AdirProgram.class
    // =========================

    public static void main(String[] args) throws Exception {


        // Read source file - My language
        var in = AdirLangCompiler.class.getResourceAsStream("/program.adir");

        if (in == null) {
            throw new RuntimeException("program.adir not found in resources");
        }
        
        String source = new String(in.readAllBytes()).trim();
        Parser parser = new Parser(source);
        List<Stmt> program = parser.parseProgram();
        

        // AdirProgram.class
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // public class AdirProgram extends Object
        cw.visit(V17,ACC_PUBLIC, "adirlanguage/runtime/ProgramAdir", null, "java/lang/Object", null);


        // constructor: public AdirProgram() { super(); }
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

        
      
        Map<String, Integer> locals = new HashMap<>();
        int[] nextLocalIndex = new int[]{1}; // slot 0 is for args


        
        for (Stmt stmt : program) {
            CodeGen.emitStmt(mv, stmt, locals, nextLocalIndex);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        // Write to AdirProgram.class
        Path out = Path.of("adirlanguage", "runtime", "ProgramAdir.class");
        Files.createDirectories(out.getParent());
        Files.write(out, cw.toByteArray());


        System.out.println("Compiled AdirLang source (program.adir) to JVM bytecode (AdirProgram.class)");
    }
}
