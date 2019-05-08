
package com.protryon.jasm.instruction.instructions;

import com.protryon.jasm.Constant;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.F;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// this is more like a psuedo instruction since we don't save it, write it, or create it

// this should probably never be used, but the autogenerator references it.

public class Wide extends Instruction {

    @Override
    public String name() {
        return "Wide";
    }

    @Override
    public int opcode() {
        return 196;
    }

    @Override
    public void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException {
        if (wide) {
            throw new UnsupportedOperationException("double wide");
        }
    }

    @Override
    public void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException {
        if (wide) {
            throw new UnsupportedOperationException("double wide");
        }
    }

    @Override
    public int pushes() {
        return 0;
    }

    @Override
    public int pops() {
        // index
        return 0;
    }

    @Override
    public String toString() {
        return "Wide";
    }

    @Override
    public Instruction fromString(String str) {
        return null;
    }

    @Override
    public boolean isControl() {
        return false;
    }
}
        