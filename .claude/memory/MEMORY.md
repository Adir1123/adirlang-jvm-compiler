# AdirLang Compiler — Project Memory

## Architecture
Pipeline: `.adir source → Lexer → Parser → AST → SemanticAnalyzer → CodeGen → .class`

All source under `app/src/main/java/`:
- `adirlanguage/AdirLangCompiler.java` — entry point, orchestrates pipeline
- `lexer/` — `Lexer.java`, `Token.java` (record), `TokenType.java`
- `parser/Parser.java` — accepts `List<Token>` (lexer and parser are separate)
- `ast/` — sealed interfaces + records: `Stmt`, `Expr`, `Op` (enum), node records
- `semantic/SemanticAnalyzer.java` — scoped variable checking
- `codegen/CodeGen.java` (instance class) + `CodeGenContext.java`

## Key design decisions (post-refactor)
- AST nodes are **records** (immutable data carriers)
- `Stmt` and `Expr` are **sealed interfaces** listing all permitted subtypes
- `BinaryExpr.op` is `Op` enum (ADD, MUL), not a raw char
- `IfStmt.elseBranch` is `Optional<List<Stmt>>` (explicit absence)
- `CodeGen` is an **instance class** bound to a `MethodVisitor` + `CodeGenContext`
- `CodeGenContext` encapsulates locals map + nextSlot counter
- `import static org.objectweb.asm.Opcodes.*` used instead of `implements Opcodes`
- Parser constructor accepts `List<Token>` (lexer called explicitly in compiler)

## Scoping semantics (SemanticAnalyzer)
- Top-level: flat sequential scope (LetStmt mutates shared defined set)
- If/else branches: each branch receives a **copy** of outer defined set
  - Declarations in branches do NOT leak into sibling branches or outer scope
  - Redeclaration in same scope is a semantic error

## Build
- `./gradlew :app:compileAdir` — compile program.adir
- `./gradlew :app:runAdirProgram` — compile + run
- Java 17 toolchain (no preview features used)
- Output: relative path `adirlanguage/runtime/ProgramAdir.class` from workingDir=`build/run`
- Derived from constant `CLASS_INTERNAL_NAME = "adirlanguage/runtime/ProgramAdir"`

## program.adir expected output
14, 21, 26
