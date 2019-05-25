package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant
import com.shapesecurity.functional.Pair

class CMethodRef(class_ref: Int, name_and_type_ref: Int) : CConstant<Pair<Int, Int>>(Pair.of(class_ref, name_and_type_ref), 10) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeShort(this.value.left)
        out.writeShort(this.value.right)
    }
}
