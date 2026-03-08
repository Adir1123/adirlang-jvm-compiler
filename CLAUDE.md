# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project
./gradlew build

# Compile program.adir to JVM bytecode only
./gradlew :app:compileAdir

# Compile and execute program.adir on the JVM
./gradlew :app:runAdirProgram

# Run tests (JUnit 5; no tests exist yet)
./gradlew test
```

The compiled output class is written to `build/run/adirlanguage/runtime/ProgramAdir.class`.

## Architecture

AdirLang is a compiler for a custom language (`.adir` files) that targets the JVM. The pipeline is:

```
.adir source → Lexer → Parser → AST → SemanticAnalyzer → CodeGen → .class file
```

All source lives under `app/src/main/java/`:

- **`adirlanguage/AdirLangCompiler.java`** — Entry point. Orchestrates the full pipeline, loads `program.adir` from JAR resources, sets up the ASM class structure, and writes the output `.class` file.
- **`lexer/`** — Tokenizes source text. Tracks line/column for error diagnostics. Recognizes keywords (`let`, `print`, `if`, `else`), operators, number literals, and identifiers.
- **`parser/Parser.java`** — Recursive descent parser. Builds AST from tokens. Operator precedence: `*` > `+`.
- **`ast/`** — AST node definitions. Statements: `LetStmt`, `PrintStmt`, `IfStmt`. Expressions: `NumberExpr`, `VarExpr`, `BinaryExpr`. All nodes carry source location (line/column).
- **`semantic/SemanticAnalyzer.java`** — Validates variable declarations and detects undefined variable usage, reporting errors with location info.
- **`codegen/CodeGen.java`** — Emits JVM bytecode via ASM. Manages local variable slot allocation. Handles arithmetic (`IADD`, `IMUL`), control flow (labels/`IFEQ`/`GOTO`), and `System.out.println` for print statements.

## Language Features (current)

- Variable declarations: `let x = expr;`
- Print statements: `print expr;`
- If/else: `if (cond) { ... } else { ... }`
- Arithmetic: `+`, `*` (integers only)
- The example program lives at `app/src/main/resources/program.adir`

## Key Dependencies

- **ASM 9.7** — JVM bytecode generation
- **Java 17+** required
- **Gradle 9.2** build system
