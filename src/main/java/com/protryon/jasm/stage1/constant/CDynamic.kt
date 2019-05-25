package com.protryon.jasm.stage1.constant

import com.protryon.jasm.stage1.CConstant
import com.shapesecurity.functional.Pair

import java.io.DataOutputStream
import java.io.IOException

class CDynamic(bootstrap_method_attr_index: Int, name_and_type_index: Int) : CConstant<Pair<Int, Int>>(Pair.of(bootstrap_method_attr_index, name_and_type_index), 17) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeShort(this.value.left)
        out.writeShort(this.value.right)
    }
}
