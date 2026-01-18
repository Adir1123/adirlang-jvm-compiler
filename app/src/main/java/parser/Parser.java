
package parser;

import ast.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {

        private final String input;
        private int index = 0;

        public Parser(String input) {
            this.input = input.replaceAll("\\s+", "");
        }


        // Public entry
        public List<Stmt> parseProgram() {
            List<Stmt> stmts = new ArrayList<>();
            
            while (peek() != '\0'){
                stmts.add(parseStatement());
            }
            return stmts;
        }


        // ---------- helpers ----------

        private char peek() {
            if (index >= input.length()) {
                return '\0';
            }
            return input.charAt(index);
        }

        
        private void consume(char c){
            if (peek() != c){
                throw new RuntimeException("Expected '" + c + "' but found '" + peek());
            }
            index++;
        }


        private boolean startsWithAt(String s) {
            return input.startsWith(s, index);
        }
    

        // ---------- Statements ----------

        private Stmt parseStatement() {
            if (startsWithAt("let")) {
                index += 3; // skip "let"
                String name = parseIdentifier();
                consume('=');
                Expr value = parseAddition();
                consume(';');
                return new LetStmt(name, value);
            }

            if (startsWithAt("print")) {
                index += 5; // skip "print"
                Expr expr = parseAddition();
                consume(';');
                return new PrintStmt(expr);
            }

            throw new RuntimeException("Unknown statement at position " + index);
        }


        private String parseIdentifier() {
            int start = index;
            while (Character.isLetter(peek())) {
                index++;
            }

            if (start == index) {
                throw new RuntimeException("Expected identifier at position " + index);
            }

            return input.substring(start, index);
        }


        // ---------- expressions ----------

        private Expr parseAddition() {  // Whole '+' Expr

            Expr expr = parseMultiplication();
            while (index < input.length() && input.charAt(index) == '+') {
                index++; // skip '+'
                Expr right = parseMultiplication();
                expr = new BinaryExpr(expr, '+', right);
            }
            return expr;
        }


        
        private Expr parseMultiplication() { // Whole '*' Expr

            Expr expr = parsePrimary();
            while (index < input.length() && input.charAt(index) == '*') {
                index++; // skip '*'
                Expr right = parsePrimary();
                expr = new BinaryExpr(expr, '*', right);
            }
            return expr;
        }


        
        private Expr parsePrimary() { // Whole '()'
            if (peek() == '(') {
                consume('(');
                Expr inside = parseAddition();
                consume(')');
                return inside;
            }

            if (Character.isDigit(peek())) {
                return parseInteger();
            }

            if (Character.isLetter(peek())) {
                String name = parseIdentifier();
                return new VarExpr(name);
            }

            throw new RuntimeException("Expected primary expression at position " + index);
        }


        private Expr parseInteger() {
            int start = index;
            while (Character.isDigit(peek())) {
                index++;
            }
            
            int value = Integer.parseInt(input.substring(start, index));
            return new NumberExpr(value);
        }
    }