package com.protryon.jasm.instruction.psuedoinstructions;

import com.protryon.jasm.Constant;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.Instruction;
import com.shapesecurity.functional.F;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Label extends Instruction {

    public final String name;

    public Label(String name) {
        this.name = name;
    }


    @Override
    public String name() {
        return ":" + name;
    }

    @Override
    public int opcode() {
        return -1;
    }

    @Override
    public void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int pushes() {
        return 0;
    }

    @Override
    public int pops() {
        return 0;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public Instruction fromString(String str) {
        return new Label(str.substring(1));
    }
}
