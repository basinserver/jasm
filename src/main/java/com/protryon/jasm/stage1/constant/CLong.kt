package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CLong(lng: Long) : CConstant<Long>(lng, 5) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeLong(this.value)
    }

    override val isDoubled: Boolean
        get() = true

}
