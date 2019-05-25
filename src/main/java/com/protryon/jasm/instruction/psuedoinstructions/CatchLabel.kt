package com.protryon.jasm.instruction.psuedoinstructions

class CatchLabel(name: String) : Label(name) {


    override fun opcode(): Int {
        return -4
    }


    override fun pushes(): Int {
        return 1
    }

}
