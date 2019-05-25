package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant
import com.shapesecurity.functional.Pair

class CNameAndType(name_ref: Int, descriptor_ref: Int) : CConstant<Pair<Int, Int>>(Pair.of(name_ref, descriptor_ref), 12) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeShort(this.value.left)
        out.writeShort(this.value.right)
    }
}
