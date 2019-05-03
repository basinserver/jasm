package com.protryon.jasm.instruction.psuedoinstructions;

public class CatchLabel extends Label {


    public CatchLabel(String name) {
        super(name);
    }


    @Override
    public int opcode() {
        return -4;
    }


    @Override
    public int pushes() {
        return 1;
    }

}
