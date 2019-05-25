package com.protryon.jasm.stage1.constant

import com.protryon.jasm.stage1.CConstant

import java.io.DataOutputStream
import java.io.IOException

class CPackage(ref: Int) : CConstant<Int>(ref, 20) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeShort(this.value)
    }

}
