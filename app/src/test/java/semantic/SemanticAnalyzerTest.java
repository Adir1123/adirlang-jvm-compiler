package semantic;

import ast.Stmt;
import lexer.Lexer;
import lexer.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import parser.Parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {

    // ---------------------------------------------------------------- helpers

    private void analyze(String source) {
        List<Token> tokens  = new Lexer(source).tokenize();
        List<Stmt>  program = new Parser(tokens).parseProgram();
        new SemanticAnalyzer().check(program);
    }

    private void assertValid(String source) {
        assertDoesNotThrow(() -> analyze(source),
                "Expected valid program to pass but it threw");
    }

    private RuntimeException assertSemanticError(String source) {
        return assertThrows(RuntimeException.class, () -> analyze(source),
                "Expected semantic error but program was accepted");
    }

    // ---------------------------------------------------------------- valid programs

    @Nested class ValidPrograms {

        @Test void variable_used_after_declaration() {
            assertValid("let x = 5; print x;");
        }

        @Test void variable_used_in_arithmetic() {
            assertValid("let x = 5; let y = x + 1; print y;");
        }

        @Test void multiple_variables_in_expression() {
            assertValid("let a = 1; let b = 2; print a + b;");
        }

        @Test void if_condition_references_outer_variable() {
            assertValid("let x = 1; if (x) { print x; }");
        }

        @Test void outer_variable_visible_in_both_branches() {
            assertValid("let x = 5; if (x) { print x; } else { print x; }");
        }

        @Test void same_variable_name_in_separate_branches_is_allowed() {
            // Each branch has its own scope — same name is not a redeclaration
            assertValid("""
                    let x = 1;
                    if (x) {
                        let y = 10;
                        print y;
                    } else {
                        let y = 20;
                        print y;
                    }
                    """);
        }

        @Test void nested_arithmetic_expressions() {
            assertValid("let a = 1; let b = 2; let c = 3; print a + b * c;");
        }
    }

    // ---------------------------------------------------------------- undefined variables

    @Nested class UndefinedVariables {

        @Test void print_of_undefined_variable_throws() {
            RuntimeException ex = assertSemanticError("print z;");
            assertTrue(ex.getMessage().contains("z"),
                    "Error should name the undefined variable");
        }

        @Test void undefined_variable_on_rhs_of_let_throws() {
            RuntimeException ex = assertSemanticError("let x = missing;");
            assertTrue(ex.getMessage().contains("missing"));
        }

        @Test void using_variable_before_its_declaration_throws() {
            // Forward references are not allowed — "x" not yet declared
            assertSemanticError("print x; let x = 5;");
        }

        @Test void error_message_contains_line_number() {
            RuntimeException ex = assertSemanticError("print undefined;");
            assertTrue(ex.getMessage().contains("1:"),
                    "Error should contain source location: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- redeclaration

    @Nested class Redeclaration {

        @Test void redeclaring_variable_in_same_scope_throws() {
            RuntimeException ex = assertSemanticError("let x = 1; let x = 2;");
            assertTrue(ex.getMessage().contains("x"),
                    "Error should name the duplicate variable");
        }

        @Test void three_declarations_of_same_name_throws_on_second() {
            assertSemanticError("let a = 1; let a = 2; let a = 3;");
        }
    }

    // ---------------------------------------------------------------- branch scoping (regression tests for fixed bug)

    @Nested class BranchScoping {

        @Test void variable_from_then_branch_is_not_visible_in_else_branch() {
            // Before the scoping fix, the same 'defined' set was shared across
            // both branches, so 'y' from the then-block would erroneously appear
            // defined when checking the else-block.
            assertSemanticError("""
                    let x = 1;
                    if (x) {
                        let y = 10;
                    } else {
                        print y;
                    }
                    """);
        }

        @Test void variable_from_then_branch_is_not_visible_after_if() {
            // A variable declared inside a branch must not exist in the outer scope.
            assertSemanticError("""
                    let x = 1;
                    if (x) {
                        let inner = 10;
                    }
                    print inner;
                    """);
        }

        @Test void variable_from_else_branch_is_not_visible_after_if() {
            assertSemanticError("""
                    let x = 1;
                    if (x) {
                        print x;
                    } else {
                        let inner = 10;
                    }
                    print inner;
                    """);
        }

        @Test void outer_scope_variable_is_visible_inside_then_branch() {
            assertValid("""
                    let outer = 5;
                    if (outer) {
                        print outer;
                    }
                    """);
        }

        @Test void outer_scope_variable_is_visible_inside_else_branch() {
            assertValid("""
                    let outer = 5;
                    if (outer) {
                        print 1;
                    } else {
                        print outer;
                    }
                    """);
        }

        @Test void outer_variable_redeclared_inside_branch_throws() {
            // 'x' is already declared in the outer scope;
            // redeclaring it in the branch should be rejected.
            assertSemanticError("""
                    let x = 1;
                    if (x) {
                        let x = 2;
                    }
                    """);
        }
    }
}
