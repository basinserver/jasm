package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant
import com.shapesecurity.functional.Pair

class CMethodHandle(type: Byte, reference_index: Int) : CConstant<Pair<Byte, Int>>(Pair.of(type, reference_index), 15) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.write(this.value.left.toInt())
        out.writeShort(this.value.right)
    }
}
