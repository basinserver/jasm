package com.protryon.jasm.instruction.instructions

import com.protryon.jasm.Constant
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.Pair

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

class Lookupswitch : Instruction() {

    var default: Label? = null
    var pairs: Array<Pair<Int, Label>>? = null

    override val isControl: Boolean
        get() = true

    override fun name(): String {
        return "Lookupswitch"
    }

    override fun opcode(): Int {
        return 171
    }

    @Throws(IOException::class)
    override fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int)->Label, pc: Int, inputStream: DataInputStream) {
        val currentOffset = pc % 4
        val toRead = 3 - currentOffset
        inputStream.read(ByteArray(toRead)) // padding
        default = labelMaker.invoke(pc + inputStream.readInt())
        val npairs = inputStream.readInt()
        if (npairs < 0) {
            throw UnsupportedOperationException("invalid lookupswitch")
        }
        pairs = arrayOfNulls<Pair<*, *>>(npairs) as Array<Pair<Int, Label>>
        for (i in 0 until npairs) {
            pairs!![i] = Pair.of(inputStream.readInt(), labelMaker.invoke(inputStream.readInt() + pc))
        }
    }

    @Throws(IOException::class)
    override fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label)->Int, constantIndexer: (Constant<*>) -> Int, pc: Int) {
        val currentOffset = pc % 4
        val toWrite = 3 - currentOffset
        out.write(ByteArray(toWrite))
        out.writeInt(labelIndexer.invoke(default!!) - pc)
        out.writeInt(pairs!!.size)
        for (pair in pairs!!) {
            out.writeInt(pair.left)
            out.writeInt(labelIndexer.invoke(pair.right) - pc)
        }
    }

    override fun pushes(): Int {
        return 0
    }

    override fun pops(): Int {
        // key
        return 1
    }

    override fun toString(): String {
        return "Lookupswitch"
    }

    override fun fromString(str: String): Instruction {
        error("unsupported")
    }
}
        