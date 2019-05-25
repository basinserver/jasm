package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CInteger(i: Int) : CConstant<Int>(i, 3) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeInt(this.value)
    }
}
