package com.protryon.jasm.instruction.instructions

import com.protryon.jasm.Constant
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.F

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

// this is more like a psuedo instruction since we don't save it, write it, or create it

// this should probably never be used, but the autogenerator references it.

class Wide : Instruction() {

    override val isControl: Boolean
        get() = false

    override fun name(): String {
        return "Wide"
    }

    override fun opcode(): Int {
        return 196
    }

    @Throws(IOException::class)
    override fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int)->Label, pc: Int, inputStream: DataInputStream) {
        if (wide) {
            throw UnsupportedOperationException("double wide")
        }
    }

    @Throws(IOException::class)
    override fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label)->Int, constantIndexer: (Constant<*>) -> Int, pc: Int) {
        if (wide) {
            throw UnsupportedOperationException("double wide")
        }
    }

    override fun pushes(): Int {
        return 0
    }

    override fun pops(): Int {
        // index
        return 0
    }

    override fun toString(): String {
        return "Wide"
    }

    override fun fromString(str: String): Instruction {
        error("unsupported")
    }
}
        