package com.protryon.jasm.stage1

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class Attribute {
    val attribute_name_index: Int
    val attribute_info: ByteArray

    @Throws(IOException::class)
    constructor(`in`: DataInputStream) : this(`in`.readUnsignedShort(), `in`) {
    }

    @Throws(IOException::class)
    protected constructor(name_index: Int, `in`: DataInputStream) {
        attribute_name_index = name_index
        val aib = ByteArray(`in`.readInt())
        `in`.readFully(aib)
        attribute_info = aib
    }

    constructor(name_index: Int, info: ByteArray) {
        this.attribute_name_index = name_index
        this.attribute_info = info
    }

    @Throws(IOException::class)
    fun write(out: DataOutputStream): Attribute {
        out.writeShort(attribute_name_index)
        out.writeInt(attribute_info.size)
        out.write(attribute_info)
        return this
    }

}
