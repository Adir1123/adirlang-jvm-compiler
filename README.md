<div align="center">
<pre>
 █████╗ ██████╗ ██╗██████╗ ██╗      █████╗ ███╗   ██╗ ██████╗ 
██╔══██╗██╔══██╗██║██╔══██╗██║     ██╔══██╗████╗  ██║██╔════╝ 
███████║██║  ██║██║██████╔╝██║     ███████║██╔██╗ ██║██║  ███╗
██╔══██║██║  ██║██║██╔══██╗██║     ██╔══██║██║╚██╗██║██║   ██║
██║  ██║██████╔╝██║██║  ██║███████╗██║  ██║██║ ╚████║╚██████╔╝
╚═╝  ╚═╝╚═════╝ ╚═╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ 
-------------------------------------------------------------
Custom programming language & JVM compiler written in Java
</pre>

![Java](https://img.shields.io/badge/Java-17+-orange)
![JVM](https://img.shields.io/badge/JVM-Bytecode-blue)
![ASM](https://img.shields.io/badge/ASM-Bytecode%20Engineering-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

</div>


**AdirLang** — a custom-designed programming language with a full compiler pipeline that translates source code directly into executable JVM `.class` files.

---

## Overview

**AdirLang** is a personal systems-level project focused on understanding how high-level languages are compiled and executed on the JVM.  
The compiler is written entirely in **Java** and generates bytecode using the **ASM** library.

---

## Language Example

Example of a simple **AdirLang** program:


```adir
let y = 2+3*4;
print y;
let x = 7;
let z = x+y;
print x+y;
if (z) {
    print z+5;
} else {
    print z;
}
```

## Features

- Custom language syntax (`.adir`)
- Dedicated Lexer with line/column tracking
- Recursive descent Parser
- Abstract Syntax Tree (AST)
- Semantic analysis (scope & validity checks
- JVM bytecode generation using ASM
- Produces executable `.class` files

---

## Compiler Pipeline

```
.adir source file
        ↓
      Lexer
        ↓
     Parsing
        ↓
       AST 
        ↓
 Semantic Analysis
        ↓
Bytecode Generation
        ↓
 JVM .class output
```

---

## Project Structure

```
src/
├── adirlanguage/        # Compiler entry point
├── lexer/               # Tokenizer (tokens, keywords, symbols, positions)
├── parser/              # Recursive descent parser
├── ast/                 # AST node definitions
├── semantic/            # Semantic analysis (validation & scoping)
├── codegen/             # JVM bytecode generation (ASM)
```

---

## Build

```sh
./gradlew build
```

---

## Tests

```sh
./gradlew test
```
tests covering the full compiler pipeline:
- **Lexer** — token recognition, line/column tracking, error cases
- **Parser** — AST structure, operator precedence, error handling
- **Semantic Analyzer** — scoping rules, undefined variables, redeclaration
- **End-to-End** — compiles and executes programs in memory, asserts output

---

## Run

```sh
.\gradlew :app:runAdirProgram
```
This task:
1. Runs the compiler on a .adir source file
2. Generates JVM bytecode into build/run
3. Executes the resulting program on the JVM

---

## Technologies

- Java 17+
- JVM
- ASM (Bytecode Engineering Library)
- Gradle

---

## Goals

- Learn compiler architecture end-to-end
- Understand JVM bytecode and execution
- Gain experience with lexical analysis, parsing, and code generation

---

## Future Work

- Comparison operators and boolean expressions
- While / for loops
- Improved error diagnostics
- Type inference
- CLI interface

---

## Author

**Adir Gabay**  
B.Sc. Computer Science  
Personal language & compiler project  

GitHub: https://github.com/Adir1123

---

## License

Distributed under the **MIT License**.  
See `LICENSE` for more information.

Test PR for n8n reviewer bot