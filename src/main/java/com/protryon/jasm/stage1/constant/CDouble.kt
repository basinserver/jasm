package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CDouble(d: Double) : CConstant<Double>(d, 6) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeDouble(this.value)
    }

    override val isDoubled: Boolean
        get() = true
}
