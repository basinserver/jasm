package com.protryon.jasm.instruction;

import com.protryon.jasm.Constant;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.F;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Instruction {

    public abstract String name();

    public abstract int opcode();

    public abstract void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException;

    public abstract void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException;

    public abstract int pushes();

    public abstract int pops();

    public abstract String toString();

    public abstract Instruction fromString(String str);

}
