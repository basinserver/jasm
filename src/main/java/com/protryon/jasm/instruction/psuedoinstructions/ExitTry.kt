package com.protryon.jasm.instruction.psuedoinstructions

import com.protryon.jasm.Constant
import com.protryon.jasm.JType
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.Instruction
import com.shapesecurity.functional.F

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

class ExitTry(catchBlock: Label) : Instruction() {

    var catchBlock: Label? = null

    override val isControl: Boolean
        get() = true

    init {
        this.catchBlock = catchBlock
    }

    override fun name(): String {
        return "EXIT_TRY"
    }

    override fun opcode(): Int {
        return -2
    }

    @Throws(IOException::class)
    override fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int) -> Label, pc: Int, `in`: DataInputStream) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label) -> Int, constantIndexer: (Constant<*>) -> Int, pc: Int) {
        throw UnsupportedOperationException()
    }

    override fun pushes(): Int {
        return 0
    }

    override fun pops(): Int {
        return 0
    }

    override fun toString(): String {
        return "EXIT_TRY " + catchBlock!!.name
    }

    override fun fromString(str: String): Instruction {
        error("unsupported")
    }
}
