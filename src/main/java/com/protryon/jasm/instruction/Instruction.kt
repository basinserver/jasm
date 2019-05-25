package com.protryon.jasm.instruction

import com.protryon.jasm.Constant
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.F

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

abstract class Instruction {

    abstract val isControl: Boolean

    abstract fun name(): String

    abstract fun opcode(): Int

    @Throws(IOException::class)
    abstract fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int)->Label, pc: Int, inputStream: DataInputStream)

    @Throws(IOException::class)
    abstract fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label)->Int, constantIndexer: (Constant<*>)->Int, pc: Int)

    abstract fun pushes(): Int

    abstract fun pops(): Int

    abstract override fun toString(): String

    abstract fun fromString(str: String): Instruction
}
