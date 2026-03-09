package compiler;

import ast.Stmt;
import codegen.CodeGen;
import lexer.Lexer;
import lexer.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import parser.Parser;
import semantic.SemanticAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * End-to-end tests for the AdirLang compiler.
 *
 * <p>Each test compiles a source string in memory, loads the resulting
 * bytecode with a fresh {@link ClassLoader}, captures {@code System.out},
 * invokes {@code main}, and asserts the printed output.
 *
 * <p>No filesystem I/O takes place — the tests are fully self-contained.
 */
class CompilerEndToEndTest {

    // ---------------------------------------------------------------- pipeline helpers

    /**
     * Runs the full compiler pipeline on {@code source} and returns the
     * newline-trimmed stdout produced by the generated program.
     */
    private String run(String source) throws Exception {
        List<Token> tokens  = new Lexer(source).tokenize();
        List<Stmt>  program = new Parser(tokens).parseProgram();
        new SemanticAnalyzer().check(program);
        byte[] bytecode = generateBytecode(program);
        return execute(bytecode);
    }

    private byte[] generateBytecode(List<Stmt> program) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V17, ACC_PUBLIC, "TestProgram", null, "java/lang/Object", null);

        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        CodeGen gen = new CodeGen(mv);
        program.forEach(gen::emitStmt);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * Loads {@code bytecode} in an isolated {@link ClassLoader}, redirects
     * {@code System.out}, invokes {@code main}, and returns the captured output
     * with trailing whitespace removed and line endings normalised to {@code \n}.
     */
    private String execute(byte[] bytecode) throws Exception {
        Class<?> cls = new InMemoryClassLoader(bytecode).loadClass("TestProgram");

        ByteArrayOutputStream buf    = new ByteArrayOutputStream();
        PrintStream            saved = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Method main = cls.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[0]);
        } finally {
            System.setOut(saved);
        }

        return buf.toString()
                  .replace("\r\n", "\n")  // normalise Windows line endings
                  .trim();
    }

    // ---------------------------------------------------------------- custom ClassLoader

    /** Loads a single class from a raw bytecode array without touching the filesystem. */
    private static class InMemoryClassLoader extends ClassLoader {
        private final byte[] bytecode;

        InMemoryClassLoader(byte[] bytecode) {
            this.bytecode = bytecode;
        }

        @Override
        protected Class<?> findClass(String name) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }

    // ================================================================ tests

    @Nested class Arithmetic {

        @Test void print_literal_number() throws Exception {
            assertEquals("42", run("print 42;"));
        }

        @Test void print_zero() throws Exception {
            assertEquals("0", run("print 0;"));
        }

        @Test void addition() throws Exception {
            assertEquals("7", run("print 3 + 4;"));
        }

        @Test void multiplication() throws Exception {
            assertEquals("12", run("print 3 * 4;"));
        }

        @Test void precedence_mul_before_add() throws Exception {
            // 2 + 3 * 4 = 2 + 12 = 14  (not 5 * 4 = 20)
            assertEquals("14", run("print 2 + 3 * 4;"));
        }

        @Test void parentheses_override_precedence() throws Exception {
            // (2 + 3) * 4 = 5 * 4 = 20
            assertEquals("20", run("print (2 + 3) * 4;"));
        }

        @Test void chained_addition() throws Exception {
            assertEquals("6", run("print 1 + 2 + 3;"));
        }

        @Test void chained_multiplication() throws Exception {
            assertEquals("24", run("print 2 * 3 * 4;"));
        }
    }

    @Nested class Variables {

        @Test void declare_and_print() throws Exception {
            assertEquals("10", run("let x = 10; print x;"));
        }

        @Test void variable_in_arithmetic() throws Exception {
            assertEquals("15", run("let x = 10; let y = 5; print x + y;"));
        }

        @Test void variable_on_rhs_of_declaration() throws Exception {
            assertEquals("11", run("let x = 10; let y = x + 1; print y;"));
        }

        @Test void multiple_print_statements() throws Exception {
            assertEquals("1\n2\n3", run("print 1; print 2; print 3;"));
        }
    }

    @Nested class IfElse {

        @Test void nonzero_condition_executes_then_branch() throws Exception {
            assertEquals("1", run("let x = 5; if (x) { print 1; }"));
        }

        @Test void zero_condition_skips_then_branch() throws Exception {
            // No output expected — then branch is not taken
            assertEquals("", run("let x = 0; if (x) { print 1; }"));
        }

        @Test void zero_condition_executes_else_branch() throws Exception {
            assertEquals("2", run("let x = 0; if (x) { print 1; } else { print 2; }"));
        }

        @Test void nonzero_condition_skips_else_branch() throws Exception {
            assertEquals("1", run("let x = 7; if (x) { print 1; } else { print 2; }"));
        }

        @Test void variable_declared_in_outer_scope_is_used_in_branch() throws Exception {
            assertEquals("42", run("let x = 42; if (x) { print x; }"));
        }

        @Test void multiple_statements_in_branch() throws Exception {
            assertEquals("1\n2", run("let x = 1; if (x) { print 1; print 2; }"));
        }
    }

    @Nested class FullProgram {

        @Test void example_program_from_resources() throws Exception {
            // Mirrors program.adir exactly — the canonical acceptance test
            String source = """
                    let y = 2+3*4;
                    print y;
                    let x = 7;
                    let z = x+y;
                    print x + y;
                    if (z) {
                        print z + 5;
                    } else {
                        print z;
                    }
                    """;
            assertEquals("14\n21\n26", run(source));
        }

        @Test void chained_variables_and_conditions() throws Exception {
            String source = """
                    let a = 3;
                    let b = a * 2;
                    let c = b + a;
                    if (c) {
                        print c;
                    }
                    """;
            assertEquals("9", run(source));
        }
    }
}
