# JASM

## Description
This project is a Java disassembler aimed at automatic analysis of JVM bytecode. It used to have rewriting functionality, and it may again in the future, but it does not at present.

## Status
This project can accurately disassemble Minecraft, which is the primary test case.

## Goals
The end goal is to write a Java decompiler and possibly deobfuscator.

## Running
This project does not have a frontend, it is designed as a library with `Classpath` as the primary entrypoint. You can hack together a test as I have done, or write a simple CLI frontend.

## API

Loading happens through the `Classpath` class. `Klass` represents a loaded and disassembled `.class` file, with all pointers to other classes, methods, constants, etc, resolved.