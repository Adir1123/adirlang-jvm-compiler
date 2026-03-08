package lexer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    // ---------------------------------------------------------------- helpers

    private List<Token> lex(String source) {
        return new Lexer(source).tokenize();
    }

    /** Lex a single-token source and return that first token. */
    private Token lexOne(String source) {
        return lex(source).get(0);
    }

    /** Return all token types from a source string. */
    private List<TokenType> types(String source) {
        return lex(source).stream().map(Token::type).toList();
    }

    // ---------------------------------------------------------------- keywords

    @Nested class Keywords {

        @Test void let_is_recognized() {
            assertEquals(TokenType.LET, lexOne("let").type());
        }

        @Test void print_is_recognized() {
            assertEquals(TokenType.PRINT, lexOne("print").type());
        }

        @Test void if_is_recognized() {
            assertEquals(TokenType.IF, lexOne("if").type());
        }

        @Test void else_is_recognized() {
            assertEquals(TokenType.ELSE, lexOne("else").type());
        }

        @Test void keyword_prefix_is_identifier_not_keyword() {
            // "letter" starts with "let" but is not the keyword
            Token t = lexOne("letter");
            assertEquals(TokenType.IDENT, t.type());
            assertEquals("letter", t.lexeme());
        }
    }

    // ---------------------------------------------------------------- symbols

    @Nested class Symbols {

        @Test void plus_is_recognized()   { assertEquals(TokenType.PLUS,   lexOne("+").type()); }
        @Test void star_is_recognized()   { assertEquals(TokenType.STAR,   lexOne("*").type()); }
        @Test void equals_is_recognized() { assertEquals(TokenType.EQUALS, lexOne("=").type()); }
        @Test void semi_is_recognized()   { assertEquals(TokenType.SEMI,   lexOne(";").type()); }
        @Test void lparen_is_recognized() { assertEquals(TokenType.LPAREN, lexOne("(").type()); }
        @Test void rparen_is_recognized() { assertEquals(TokenType.RPAREN, lexOne(")").type()); }
        @Test void lbrace_is_recognized() { assertEquals(TokenType.LBRACE, lexOne("{").type()); }
        @Test void rbrace_is_recognized() { assertEquals(TokenType.RBRACE, lexOne("}").type()); }

        @Test void symbol_lexeme_matches_source_character() {
            assertEquals("+", lexOne("+").lexeme());
            assertEquals("*", lexOne("*").lexeme());
        }
    }

    // ---------------------------------------------------------------- literals

    @Nested class Literals {

        @Test void integer_literal_has_correct_value() {
            Token t = lexOne("42");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(42, t.intValue());
            assertEquals("42", t.lexeme());
        }

        @Test void zero_literal() {
            Token t = lexOne("0");
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(0, t.intValue());
        }

        @Test void large_valid_integer() {
            Token t = lexOne("2147483647"); // Integer.MAX_VALUE
            assertEquals(Integer.MAX_VALUE, t.intValue());
        }

        @Test void integer_overflow_throws_lex_error() {
            assertThrows(RuntimeException.class, () -> lex("99999999999999999999"));
        }
    }

    // ---------------------------------------------------------------- identifiers

    @Nested class Identifiers {

        @Test void simple_identifier() {
            Token t = lexOne("x");
            assertEquals(TokenType.IDENT, t.type());
            assertEquals("x", t.lexeme());
        }

        @Test void identifier_with_digits_and_underscore() {
            Token t = lexOne("my_var1");
            assertEquals(TokenType.IDENT, t.type());
            assertEquals("my_var1", t.lexeme());
        }

        @Test void identifier_starting_with_underscore() {
            Token t = lexOne("_hidden");
            assertEquals(TokenType.IDENT, t.type());
        }

        @Test void digit_followed_by_letters_is_two_tokens() {
            // "1abc" → NUMBER(1), IDENT(abc)
            List<Token> tokens = lex("1abc");
            assertEquals(TokenType.NUMBER, tokens.get(0).type());
            assertEquals(TokenType.IDENT,  tokens.get(1).type());
            assertEquals("abc", tokens.get(1).lexeme());
        }
    }

    // ---------------------------------------------------------------- whitespace and structure

    @Nested class WhitespaceAndStructure {

        @Test void whitespace_between_tokens_is_skipped() {
            List<TokenType> t = types("let   x");
            assertEquals(TokenType.LET,  t.get(0));
            assertEquals(TokenType.IDENT, t.get(1));
        }

        @Test void tabs_and_carriage_returns_are_skipped() {
            assertEquals(TokenType.LET, lexOne("\t\rlet").type());
        }

        @Test void last_token_is_always_eof() {
            List<Token> tokens = lex("let x = 1;");
            assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type());
        }

        @Test void empty_source_produces_only_eof() {
            List<Token> tokens = lex("");
            assertEquals(1, tokens.size());
            assertEquals(TokenType.EOF, tokens.get(0).type());
        }

        @Test void full_let_statement_produces_correct_token_sequence() {
            List<TokenType> t = types("let x = 42;");
            assertEquals(List.of(
                    TokenType.LET, TokenType.IDENT, TokenType.EQUALS,
                    TokenType.NUMBER, TokenType.SEMI, TokenType.EOF), t);
        }
    }

    // ---------------------------------------------------------------- source locations

    @Nested class SourceLocations {

        @Test void first_token_is_at_line_1_col_1() {
            Token t = lexOne("let");
            assertEquals(1, t.line());
            assertEquals(1, t.col());
        }

        @Test void column_advances_across_a_line() {
            List<Token> tokens = lex("let x = 5;");
            assertEquals(1,  tokens.get(0).col()); // let
            assertEquals(5,  tokens.get(1).col()); // x
            assertEquals(7,  tokens.get(2).col()); // =
            assertEquals(9,  tokens.get(3).col()); // 5
            assertEquals(10, tokens.get(4).col()); // ;
        }

        @Test void newline_increments_line_and_resets_column() {
            List<Token> tokens = lex("let\nx");
            Token x = tokens.get(1);
            assertEquals(2, x.line());
            assertEquals(1, x.col());
        }

        @Test void error_message_includes_location() {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> lex("\n@"));
            assertTrue(ex.getMessage().contains("2:1"),
                    "Expected '2:1' in message but got: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- error cases

    @Nested class ErrorCases {

        @Test void unexpected_character_throws() {
            assertThrows(RuntimeException.class, () -> lex("@"));
        }

        @Test void hash_symbol_throws() {
            assertThrows(RuntimeException.class, () -> lex("#"));
        }
    }
}
