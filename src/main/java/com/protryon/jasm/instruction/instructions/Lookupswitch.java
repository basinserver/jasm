
package com.protryon.jasm.instruction.instructions;

import com.protryon.jasm.Constant;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.F;
import com.shapesecurity.functional.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Lookupswitch extends Instruction {

    public Label _default;
    public Pair<Integer, Label>[] pairs;
    
    @Override
    public String name() {
        return "Lookupswitch";
    }

    @Override
    public int opcode() {
        return 171;
    }

    @Override
    public void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException {
        int currentOffset = pc % 4;
        int toRead = 3 - currentOffset;
        in.read(new byte[toRead]); // padding
        _default = labelMaker.apply(pc + in.readInt());
        int npairs = in.readInt();
        if (npairs < 0) {
            throw new UnsupportedOperationException("invalid lookupswitch");
        }
        pairs = (Pair<Integer, Label>[]) new Pair[npairs];
        for (int i = 0; i < npairs; ++i) {
            pairs[i] = Pair.of(in.readInt(), labelMaker.apply(in.readInt() + pc));
        }
    }

    @Override
    public void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException {
        int currentOffset = pc % 4;
        int toWrite = 3 - currentOffset;
        out.write(new byte[toWrite]);
        out.writeInt(labelIndexer.apply(_default) - pc);
        out.writeInt(pairs.length);
        for (Pair<Integer, Label> pair : pairs) {
            out.writeInt(pair.left);
            out.writeInt(labelIndexer.apply(pair.right) - pc);
        }
    }

    @Override
    public int pushes() {
        return 0;
    }

    @Override
    public int pops() {
        // key
        return 1;
    }

    @Override
    public String toString() {
        return "Lookupswitch";
    }

    @Override
    public Instruction fromString(String str) {
        return null;
    }

    @Override
    public boolean isControl() {
        return true;
    }
}
        