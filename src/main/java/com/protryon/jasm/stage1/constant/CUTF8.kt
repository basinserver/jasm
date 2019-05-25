package com.protryon.jasm.stage1.constant

import java.io.DataOutputStream
import java.io.IOException

import com.protryon.jasm.stage1.CConstant

class CUTF8(value: String) : CConstant<String>(value, 1) {

    @Throws(IOException::class)
    override fun write(out: DataOutputStream) {
        val utf = this.value.toByteArray()
        out.writeShort(utf.size)
        out.write(utf)
    }

    companion object {

        fun assertCUTF8(CConstant: CConstant<*>): CUTF8 {
            if (CConstant !is CUTF8) {
                throw RuntimeException("Expected UTF8!")
            }
            return CConstant
        }
    }
}
