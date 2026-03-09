package adirlanguage;

import ast.Stmt;
import codegen.CodeGen;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Entry point for the AdirLang compiler.
 *
 * <p>Orchestrates the full compilation pipeline:
 * <pre>
 *   .adir source → Lexer → Parser → SemanticAnalyzer → CodeGen → .class file
 * </pre>
 *
 * <p>The output {@code .class} file is written to a path relative to the
 * JVM working directory.  When invoked via the Gradle {@code compileAdir}
 * task, that working directory is {@code build/run/}, placing the output at
 * {@code build/run/adirlanguage/runtime/ProgramAdir.class}.
 */
public class AdirLangCompiler {

    /** Internal JVM class name of the generated class. */
    private static final String CLASS_INTERNAL_NAME = "adirlanguage/runtime/ProgramAdir";

    /**
     * Output path for the {@code .class} file, relative to the working
     * directory.  Derived from {@link #CLASS_INTERNAL_NAME} to keep the two
     * in sync automatically.
     */
    private static final Path OUTPUT_PATH = Path.of(CLASS_INTERNAL_NAME + ".class");

    public static void main(String[] args) throws Exception {
        String source = loadSource();

        List<Token> tokens  = new Lexer(source).tokenize();
        List<Stmt>  program = new Parser(tokens).parseProgram();
        new SemanticAnalyzer().check(program);

        byte[] bytecode = generateBytecode(program);
        Files.createDirectories(OUTPUT_PATH.getParent());
        Files.write(OUTPUT_PATH, bytecode);
    }

    // ----------------------------------------------------------------- Source loading

    private static String loadSource() throws Exception {
        try (var in = AdirLangCompiler.class.getResourceAsStream("/program.adir")) {
            if (in == null) throw new RuntimeException("program.adir not found in resources");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    // ----------------------------------------------------------------- Bytecode generation

    private static byte[] generateBytecode(List<Stmt> program) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC, CLASS_INTERNAL_NAME, null, "java/lang/Object", null);

        emitDefaultConstructor(cw);
        emitMainMethod(cw, program);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** Emits the implicit no-arg constructor: {@code public ProgramAdir() { super(); }}. */
    private static void emitDefaultConstructor(ClassWriter cw) {
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
    }

    /** Emits {@code public static void main(String[] args)} containing the compiled program. */
    private static void emitMainMethod(ClassWriter cw, List<Stmt> program) {
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        CodeGen gen = new CodeGen(mv);
        program.forEach(gen::emitStmt);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
