package com.protryon.jasm.stage1.constant

import com.protryon.jasm.stage1.CConstant

import java.io.DataOutputStream
import java.io.IOException

class CString(ref: Int) : CConstant<Int>(ref, 8) {

    @Throws(IOException::class)
    override fun write(out: DataOutputStream) {
        out.writeShort(this.value)
    }
}
