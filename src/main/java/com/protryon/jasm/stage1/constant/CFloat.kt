package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CFloat(f: Float) : CConstant<Float>(f, 4) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeFloat(this.value)
    }
}
