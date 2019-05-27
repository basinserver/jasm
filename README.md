# JASM

## Description
This project is a JVM disassembler and Java decompiler written in Kotlin.

## Status
Some features are not yet completed in the decompiler, but the disassembler is complete.

## Goals
* Disassembler Minecraft and general JVM bytecode
* Decompile said bytecode into human-readable and compilable Java.
* Create an ecosystem for reverse engineering compiled Java code.

## Running
This project does not have a frontend, it is designed as a library with `Classpath` as the primary entrypoint. You can hack together a test as I have done, or write a simple CLI frontend.

## API
Loading happens through the `Classpath` class. `Klass` represents a loaded and disassembled `.class` file, with all pointers to other classes, methods, constants, etc, resolved.