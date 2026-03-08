package parser;

import ast.*;
import lexer.Lexer;
import lexer.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    // ---------------------------------------------------------------- helpers

    private List<Stmt> parse(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        return new Parser(tokens).parseProgram();
    }

    /** Parse exactly one statement and return it. */
    private Stmt parseOne(String source) {
        List<Stmt> stmts = parse(source);
        assertEquals(1, stmts.size(), "Expected exactly one statement");
        return stmts.get(0);
    }

    // ---------------------------------------------------------------- statements

    @Nested class Statements {

        @Test void let_statement_name_and_value() {
            LetStmt s = (LetStmt) parseOne("let x = 5;");
            assertEquals("x", s.name());
            assertEquals(5, ((NumberExpr) s.value()).value());
        }

        @Test void let_statement_location_points_to_let_keyword() {
            LetStmt s = (LetStmt) parseOne("let x = 5;");
            assertEquals(1, s.line());
            assertEquals(1, s.col());
        }

        @Test void print_statement_wraps_expression() {
            PrintStmt s = (PrintStmt) parseOne("print 42;");
            assertEquals(42, ((NumberExpr) s.expr()).value());
        }

        @Test void print_of_variable_reference() {
            PrintStmt s = (PrintStmt) parseOne("print myVar;");
            assertEquals("myVar", ((VarExpr) s.expr()).name());
        }

        @Test void multiple_top_level_statements() {
            assertEquals(3, parse("let x = 1; let y = 2; print x;").size());
        }

        @Test void if_without_else_has_empty_optional() {
            IfStmt s = (IfStmt) parseOne("if (x) { print x; }");
            assertTrue(s.elseBranch().isEmpty());
            assertEquals(1, s.thenBranch().size());
        }

        @Test void if_with_else_has_present_optional() {
            IfStmt s = (IfStmt) parseOne("if (x) { print 1; } else { print 2; }");
            assertTrue(s.elseBranch().isPresent());
            assertEquals(1, s.elseBranch().get().size());
        }

        @Test void multiple_statements_inside_block() {
            IfStmt s = (IfStmt) parseOne("if (x) { let a = 1; let b = 2; print a; }");
            assertEquals(3, s.thenBranch().size());
        }

        @Test void empty_then_block_is_valid() {
            IfStmt s = (IfStmt) parseOne("if (x) {}");
            assertEquals(0, s.thenBranch().size());
        }
    }

    // ---------------------------------------------------------------- expressions

    @Nested class Expressions {

        @Test void number_literal_value() {
            PrintStmt s = (PrintStmt) parseOne("print 99;");
            assertEquals(99, ((NumberExpr) s.expr()).value());
        }

        @Test void addition_produces_add_op() {
            BinaryExpr e = (BinaryExpr) ((PrintStmt) parseOne("print 1 + 2;")).expr();
            assertEquals(Op.ADD, e.op());
            assertEquals(1, ((NumberExpr) e.left()).value());
            assertEquals(2, ((NumberExpr) e.right()).value());
        }

        @Test void multiplication_produces_mul_op() {
            BinaryExpr e = (BinaryExpr) ((PrintStmt) parseOne("print 3 * 4;")).expr();
            assertEquals(Op.MUL, e.op());
        }

        @Test void multiplication_binds_tighter_than_addition() {
            // "2 + 3 * 4" must parse as "2 + (3 * 4)", not "(2 + 3) * 4"
            BinaryExpr top = (BinaryExpr) ((PrintStmt) parseOne("print 2 + 3 * 4;")).expr();

            assertEquals(Op.ADD, top.op());
            assertEquals(2, ((NumberExpr) top.left()).value());

            BinaryExpr rhs = (BinaryExpr) top.right();
            assertEquals(Op.MUL, rhs.op());
            assertEquals(3, ((NumberExpr) rhs.left()).value());
            assertEquals(4, ((NumberExpr) rhs.right()).value());
        }

        @Test void parentheses_force_addition_before_multiplication() {
            // "(2 + 3) * 4" must parse as "(2 + 3) * 4"
            BinaryExpr top = (BinaryExpr) ((PrintStmt) parseOne("print (2 + 3) * 4;")).expr();

            assertEquals(Op.MUL, top.op());
            BinaryExpr lhs = (BinaryExpr) top.left();
            assertEquals(Op.ADD, lhs.op());
            assertEquals(4, ((NumberExpr) top.right()).value());
        }

        @Test void addition_is_left_associative() {
            // "1 + 2 + 3" must parse as "(1 + 2) + 3"
            BinaryExpr top = (BinaryExpr) ((PrintStmt) parseOne("print 1 + 2 + 3;")).expr();

            assertEquals(Op.ADD, top.op());
            assertInstanceOf(BinaryExpr.class, top.left(),  "left of + should be (1+2)");
            assertInstanceOf(NumberExpr.class, top.right(), "right of + should be 3");
        }

        @Test void multiplication_is_left_associative() {
            // "2 * 3 * 4" must parse as "(2 * 3) * 4"
            BinaryExpr top = (BinaryExpr) ((PrintStmt) parseOne("print 2 * 3 * 4;")).expr();

            assertEquals(Op.MUL, top.op());
            assertInstanceOf(BinaryExpr.class, top.left());
            assertInstanceOf(NumberExpr.class, top.right());
        }

        @Test void nested_parentheses() {
            // Should parse without error
            assertDoesNotThrow(() -> parseOne("print ((1 + 2) * (3 + 4));"));
        }
    }

    // ---------------------------------------------------------------- error cases

    @Nested class ErrorCases {

        @Test void missing_semicolon_after_let_throws() {
            assertThrows(RuntimeException.class, () -> parse("let x = 5"));
        }

        @Test void missing_semicolon_after_print_throws() {
            assertThrows(RuntimeException.class, () -> parse("print 5"));
        }

        @Test void missing_equals_in_let_throws() {
            assertThrows(RuntimeException.class, () -> parse("let x 5;"));
        }

        @Test void missing_opening_paren_in_if_throws() {
            assertThrows(RuntimeException.class, () -> parse("if x) { print x; }"));
        }

        @Test void missing_closing_paren_in_if_throws() {
            assertThrows(RuntimeException.class, () -> parse("if (x { print x; }"));
        }

        @Test void missing_closing_brace_throws() {
            assertThrows(RuntimeException.class, () -> parse("if (x) { print x;"));
        }

        @Test void unknown_statement_keyword_throws() {
            assertThrows(RuntimeException.class, () -> parse("foo bar;"));
        }

        @Test void error_message_includes_source_location() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> parse("let x = ;"));
            assertTrue(ex.getMessage().contains("1:"),
                    "Expected line number in error: " + ex.getMessage());
        }
    }
}
