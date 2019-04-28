
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

public class Tableswitch extends Instruction {

    Label _default;
    int low;
    int high;
    Label[] offsets;
    
    @Override
    public String name() {
        return "Tableswitch";
    }

    @Override
    public int opcode() {
        return 170;
    }

    @Override
    public void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException {
        int currentOffset = pc % 4;
        int toRead = 3 - currentOffset;
        in.read(new byte[toRead]); // padding
        _default = labelMaker.apply(pc + in.readInt());
        low = in.readInt();
        high = in.readInt();
        int count = high - low + 1;
        if (count < 0) {
            throw new UnsupportedOperationException("invalid tableswitch");
        }
        offsets = new Label[count];
        for (int i = 0; i < count; ++i) {
            offsets[i] = labelMaker.apply(pc + in.readInt());
        }
    }

    @Override
    public void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException {
        int currentOffset = pc % 4;
        int toWrite = 3 - currentOffset;
        out.write(new byte[toWrite]);
        out.writeInt(labelIndexer.apply(_default) - pc);
        out.writeInt(low);
        out.writeInt(high);
        for (Label offset : offsets) {
            out.writeInt(labelIndexer.apply(offset) - pc);
        }
    }

    @Override
    public int pushes() {
        return 0;
    }

    @Override
    public int pops() {
        // index
        return 1;
    }

    @Override
    public String toString() {
        return "Tableswitch";
    }

    @Override
    public Instruction fromString(String str) {
        return null;
    }

}
        