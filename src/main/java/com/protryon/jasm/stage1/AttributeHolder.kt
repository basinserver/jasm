package com.protryon.jasm.stage1

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

class AttributeHolder protected constructor(var name: Int, var descriptor: Int) {

    var accessFlags = 0
    var attributes = ArrayList<Attribute>()

    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        out.writeShort(accessFlags)
        out.writeShort(name)
        out.writeShort(descriptor)
        out.writeShort(attributes.size)
        for (attr in attributes) {
            attr.write(out)
        }
    }

    companion object {

        @Throws(IOException::class)
        fun read(`in`: DataInputStream): AttributeHolder {
            val accessFlags = `in`.readUnsignedShort()
            val name_index = `in`.readUnsignedShort()
            val descriptor_index = `in`.readUnsignedShort()
            val field = AttributeHolder(name_index, descriptor_index)
            field.accessFlags = accessFlags

            val attributeCount = `in`.readUnsignedShort()
            field.attributes.ensureCapacity(attributeCount)
            for (i in 0 until attributeCount) {
                field.attributes.add(Attribute(`in`))
            }
            return field
        }
    }
}
