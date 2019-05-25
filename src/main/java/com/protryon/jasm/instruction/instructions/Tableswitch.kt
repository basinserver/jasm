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

class Tableswitch : Instruction() {

    var default: Label? = null
    var low: Int = 0
    var high: Int = 0
    var offsets: Array<Label>? = null

    override val isControl: Boolean
        get() = true

    override fun name(): String {
        return "Tableswitch"
    }

    override fun opcode(): Int {
        return 170
    }

    @Throws(IOException::class)
    override fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int)->Label, pc: Int, inputStream: DataInputStream) {
        val currentOffset = pc % 4
        val toRead = 3 - currentOffset
        inputStream.read(ByteArray(toRead)) // padding
        default = labelMaker.invoke(pc + inputStream.readInt())
        low = inputStream.readInt()
        high = inputStream.readInt()
        val count = high - low + 1
        if (count < 0) {
            throw UnsupportedOperationException("invalid tableswitch")
        }
        offsets = Array(count) {
            labelMaker.invoke(pc + inputStream.readInt())
        }
    }

    @Throws(IOException::class)
    override fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label)->Int, constantIndexer: (Constant<*>) -> Int, pc: Int) {
        val currentOffset = pc % 4
        val toWrite = 3 - currentOffset
        out.write(ByteArray(toWrite))
        out.writeInt(labelIndexer.invoke(default!!) - pc)
        out.writeInt(low)
        out.writeInt(high)
        for (offset in offsets!!) {
            out.writeInt(labelIndexer.invoke(offset) - pc)
        }
    }

    override fun pushes(): Int {
        return 0
    }

    override fun pops(): Int {
        // index
        return 1
    }

    override fun toString(): String {
        return "Tableswitch"
    }

    override fun fromString(str: String): Instruction {
        error("unsupported")
    }
}
        