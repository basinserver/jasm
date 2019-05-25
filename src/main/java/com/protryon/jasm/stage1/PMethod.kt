package com.protryon.jasm.stage1


import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList

internal class PMethod(var name_index: Int, var descriptor_index: Int) {

    private var accessFlags = 0
    var attributes = ArrayList<Attribute>()

    var isPublic: Boolean
        get() = accessFlags and 0x0001 == 0x0001
        set(n) {
            val c = isPublic
            if (c && !n) {
                accessFlags = accessFlags - 0x0001
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0001
            }
        }

    var isPrivate: Boolean
        get() = accessFlags and 0x0002 == 0x0002
        set(n) {
            val c = isPrivate
            if (c && !n) {
                accessFlags = accessFlags - 0x0002
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0002
            }
        }

    var isProtected: Boolean
        get() = accessFlags and 0x0004 == 0x0004
        set(n) {
            val c = isProtected
            if (c && !n) {
                accessFlags = accessFlags - 0x0004
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0004
            }
        }

    var isStatic: Boolean
        get() = accessFlags and 0x0008 == 0x0008
        set(n) {
            val c = isStatic
            if (c && !n) {
                accessFlags = accessFlags - 0x0008
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0008
            }
        }

    var isFinal: Boolean
        get() = accessFlags and 0x0010 == 0x0010
        set(n) {
            val c = isFinal
            if (c && !n) {
                accessFlags = accessFlags - 0x0010
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0010
            }
        }

    var isSynchronized: Boolean
        get() = accessFlags and 0x0020 == 0x0020
        set(n) {
            val c = isSynchronized
            if (c && !n) {
                accessFlags = accessFlags - 0x0020
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0020
            }
        }

    var isBridge: Boolean
        get() = accessFlags and 0x0040 == 0x0040
        set(n) {
            val c = isBridge
            if (c && !n) {
                accessFlags = accessFlags - 0x0040
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0040
            }
        }

    var isVarArgs: Boolean
        get() = accessFlags and 0x0080 == 0x0080
        set(n) {
            val c = isVarArgs
            if (c && !n) {
                accessFlags = accessFlags - 0x0080
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0080
            }
        }

    var isNative: Boolean
        get() = accessFlags and 0x0100 == 0x0100
        set(n) {
            val c = isNative
            if (c && !n) {
                accessFlags = accessFlags - 0x0100
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0100
            }
        }

    var isAbstract: Boolean
        get() = accessFlags and 0x0400 == 0x0400
        set(n) {
            val c = isAbstract
            if (c && !n) {
                accessFlags = accessFlags - 0x0400
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0400
            }
        }

    var isStrict: Boolean
        get() = accessFlags and 0x0800 == 0x0800
        set(n) {
            val c = isAbstract
            if (c && !n) {
                accessFlags = accessFlags - 0x0800
            } else if (!c && n) {
                accessFlags = accessFlags + 0x0800
            }
        }

    var isSynthetic: Boolean
        get() = accessFlags and 0x1000 == 0x1000
        set(n) {
            val c = isSynthetic
            if (c && !n) {
                accessFlags = accessFlags - 0x1000
            } else if (!c && n) {
                accessFlags = accessFlags + 0x1000
            }
        }

    @Throws(IOException::class)
    protected fun write(out: DataOutputStream) {
        out.writeShort(accessFlags)
        out.writeShort(name_index)
        out.writeShort(descriptor_index)
        out.writeShort(attributes.size)
        for (i in attributes.indices) {
            val attr = attributes[i] ?: continue
            attr.write(out)
        }
    }

    companion object {

        @Throws(IOException::class)
        protected fun read(`in`: DataInputStream): PMethod {
            val accessFlags = `in`.readUnsignedShort()
            val name_index = `in`.readUnsignedShort()
            val descriptor_index = `in`.readUnsignedShort()

            val method = PMethod(name_index, descriptor_index)

            val attribute_count = `in`.readUnsignedShort()

            for (i in 0 until attribute_count) {
                method.attributes.add(Attribute(`in`))
            }
            return method
        }
    }
}
