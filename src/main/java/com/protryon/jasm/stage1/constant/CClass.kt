package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CClass(ref: Int) : CConstant<Int>(ref, 7) {

    @Throws(IOException::class)
    public override fun write(out: DataOutputStream) {
        out.writeShort(this.value)
    }

    companion object {

        fun assertCClass(CConstant: CConstant<*>): CClass {
            if (CConstant !is CClass) {
                throw RuntimeException("Expected CClass!")
            }
            return CConstant
        }
    }

}
