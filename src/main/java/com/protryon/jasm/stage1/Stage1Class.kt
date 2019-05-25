package com.protryon.jasm.stage1

import com.protryon.jasm.*
import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.OpcodeTable
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.protryon.jasm.stage1.constant.CClass
import com.protryon.jasm.stage1.constant.CUTF8
import com.protryon.jasm.util.TrackingByteArrayInputStream

import java.io.*
import java.util.*

class Stage1Class @Throws(IOException::class) constructor(inStream: DataInputStream, val isLibrary: Boolean) {

    private var minorVersion: Int = 0
    private var majorVersion: Int = 0
    private val constants = ArrayList<CConstant<*>>()
    private val fields = ArrayList<AttributeHolder>()
    private val methods = ArrayList<AttributeHolder>()
    private val attributes = ArrayList<Attribute>()
    private val implementing = ArrayList<Int>()
    var bootstrapMethods = arrayOfNulls<CBootstrapMethod>(0)
    private var name: Int = 0
    private var extending: Int = 0
    private var accessFlags: Int = 0

    companion object {
        private val placeholderCConstant = object : CConstant<Unit>(Unit, -1) {
            override fun write(out: DataOutputStream) {
                throw UnsupportedOperationException()
            }
        }

        private val placeholderConstant = Constant(Unit)
    }

    init {
        if (inStream.read() != 0x000000CA || inStream.read() != 0x000000FE || inStream.read() != 0x000000BA || inStream.read() != 0x000000BE) {
            throw IOException("Not a Class File! Magic is not 0xCAFEBABE.")
        }
        minorVersion = inStream.readUnsignedShort()
        majorVersion = inStream.readUnsignedShort()
        val constantCount = inStream.readUnsignedShort()
        this.constants.ensureCapacity(constantCount)
        this.constants.add(placeholderCConstant)
        run {
            var i = 1
            while (i < constantCount) {
                val c = CConstant.read(inStream)
                this.constants.add(c)
                if (c.isDoubled) {
                    this.constants.add(c)
                    ++i
                }
                ++i
            }
        }
        accessFlags = inStream.readUnsignedShort()
        this.name = inStream.readUnsignedShort()
        this.extending = inStream.readUnsignedShort()
        val interface_count = inStream.readUnsignedShort()
        this.implementing.ensureCapacity(interface_count)
        for (i in 0 until interface_count) {
            this.implementing.add(inStream.readUnsignedShort())
        }
        val fieldCount = inStream.readUnsignedShort()
        for (i in 0 until fieldCount) {
            this.fields.add(AttributeHolder.read(inStream))
        }
        val methodCount = inStream.readUnsignedShort()
        for (i in 0 until methodCount) {
            this.methods.add(AttributeHolder.read(inStream))
        }
        val attributeCount = inStream.readUnsignedShort()
        for (i in 0 until attributeCount) {
            val attribute = Attribute(inStream)
            if (CUTF8.assertCUTF8(this.constants[attribute.attribute_name_index]).value == "BootstrapMethods") {
                val attributeIn = DataInputStream(ByteArrayInputStream(attribute.attribute_info))
                this.bootstrapMethods = arrayOfNulls(attributeIn.readUnsignedShort())
                for (j in this.bootstrapMethods.indices) {
                    val methodHandle = attributeIn.readUnsignedShort()
                    val arguments = IntArray(attributeIn.readUnsignedShort())
                    for (k in arguments.indices) {
                        arguments[k] = attributeIn.readUnsignedShort()
                    }
                    this.bootstrapMethods[j] = CBootstrapMethod(methodHandle, arguments)
                }
            }
            this.attributes.add(attribute)
        }
    }

    @Throws(IOException::class)
    fun write(out: DataOutputStream) {
        out.write(0x000000CA)
        out.write(0x000000FE)
        out.write(0x000000BA)
        out.write(0x000000BE)
        out.writeShort(minorVersion)
        out.writeShort(majorVersion)
        out.writeShort(constants.size)
        var lastDoubled = false
        for (CConstant in constants) {
            if (CConstant.isDoubled) {
                if (!lastDoubled) {
                    lastDoubled = true
                } else {
                    lastDoubled = false
                    continue
                }
                out.write(CConstant.type)
            }
            CConstant.write(out)
        }
        out.writeShort(accessFlags)
        out.writeShort(name)
        out.writeShort(extending)
        out.writeShort(implementing.size)
        for (i in implementing) {
            out.writeShort(i)
        }
        out.writeShort(fields.size)
        for (field in this.fields) {
            field.write(out)
        }
        out.writeShort(methods.size)
        for (method in methods) {
            method.write(out)
        }
        out.writeShort(attributes.size)
        for (attribute in attributes) {
            attribute.write(out)
        }
    }

    private fun resolveKlass(classpath: Classpath, classRef: Int): Klass {
        return classpath.loadKlass(CUTF8.assertCUTF8(this.constants[CClass.assertCClass(this.constants[classRef]).value]).value)
    }

    private fun resolveConstant(classpath: Classpath, constants: ArrayList<Constant<*>>, x: Int): Constant<*> {
        return if (constants[x] == placeholderConstant) {
            val c = this.constants[x].toConstant(classpath, this) { resolveConstant(classpath, constants, it) }
            constants[x] = c
            c
        } else {
            constants[x]
        }
    }

    @Throws(IOException::class)
    fun finishClass(classpath: Classpath, klass: Klass) {
        val constants = ArrayList<Constant<*>>(this.constants.size)
        for (i in this.constants.indices) {
            constants.add(placeholderConstant)
        }
        for (i in 1 until this.constants.size) {
            resolveConstant(classpath, constants, i)
        }
        for (ourField in this.fields) {
            val newField = klass.fields[CUTF8.assertCUTF8(this.constants[ourField.name]).value]!!

            if (newField.isStatic) {
                for (attribute in ourField.attributes) {
                    if (CUTF8.assertCUTF8(this.constants[attribute.attribute_name_index]).value == "ConstantValue") {
                        val data = attribute.attribute_info
                        if (data.size != 2) {
                            break
                        }
                        val value = java.lang.Byte.toUnsignedInt(data[0]) shl 8 or java.lang.Byte.toUnsignedInt(data[1])
                        newField.constantValue = constants[value]
                        break
                    }
                }
            }
        }

        for (i in this.methods.indices) {
            val ourMethod = this.methods[i]
            val newMethod = klass.methods[i]

            for (attribute in ourMethod.attributes) {
                if (CUTF8.assertCUTF8(this.constants[attribute.attribute_name_index]).value == "Code") {
                    val data = attribute.attribute_info
                    if (data.size < 10) {
                        break
                    }
                    val trackingIn = TrackingByteArrayInputStream(data)
                    val inputStream = DataInputStream(trackingIn)
                    val maxStack = inputStream.readUnsignedShort()
                    val maxLocals = inputStream.readUnsignedShort()
                    val codeLength = inputStream.readInt()
                    var wide = false
                    val labelsToAdd = HashMap<Int, Label>()
                    val instructionPCToIndex = HashMap<Int, Int>()
                    var pc = 0
                    while (pc < codeLength) {
                        val op = inputStream.read()
                        if (op < 0 || op >= OpcodeTable.suppliers.size) {
                            throw UnsupportedOperationException("Illegal opcode: $op")
                        }
                        if (op == 196 && !wide) { // Wide instruction
                            wide = true
                            pc = trackingIn.position - 8
                            continue
                        }
                        val ins = OpcodeTable.suppliers[op].invoke()
                        ins.read(wide, constants, newMethod, { x ->
                            val label = newMethod.getOrMakeLabel("l_$x")
                            labelsToAdd[x] = label
                            label
                        }, pc, inputStream)
                        wide = false
                        instructionPCToIndex[pc] = newMethod.code.size
                        newMethod.code.add(ins)
                        pc = trackingIn.position - 8
                    }
                    val instructionsToAdd = HashMap<Int, LinkedList<Instruction>>()
                    val exceptionTableCount = inputStream.readUnsignedShort()
                    for (j in 0 until exceptionTableCount) {
                        val start = inputStream.readUnsignedShort()
                        val end = inputStream.readUnsignedShort()
                        val handler = inputStream.readUnsignedShort()
                        val type = inputStream.readUnsignedShort()
                        val startIndex = instructionPCToIndex[start]!!
                        val endIndex = if (end == codeLength) -1 else instructionPCToIndex[end]!!
                        val handlerIndex = instructionPCToIndex[handler]!!
                        val jtype: JType
                        jtype = if (type == 0) {
                            JType.instance(classpath.loadKlass("java/lang/Exception"))
                        } else {
                            constants[type].value as JType
                        }
                        val label = newMethod.makeCatch("_catch_" + startIndex + "_" + endIndex + "_" + jtype.javaName)
                        val enter = EnterTry(label, jtype)
                        val exit = ExitTry(label)
                        (instructionsToAdd as java.util.Map<Int, LinkedList<Instruction>>).computeIfAbsent(startIndex) { x -> LinkedList() }.addLast(enter)
                        (instructionsToAdd as java.util.Map<Int, LinkedList<Instruction>>).computeIfAbsent(handlerIndex) { x -> LinkedList() }.addLast(label)
                        if (endIndex == -1) {
                            newMethod.code.add(exit)
                        } else {
                            (instructionsToAdd as java.util.Map<Int, LinkedList<Instruction>>).computeIfAbsent(endIndex) { x -> LinkedList() }.addFirst(exit)
                        }
                    }
                    labelsToAdd.forEach { pc, label -> (instructionsToAdd as java.util.Map<Int, LinkedList<Instruction>>).computeIfAbsent(instructionPCToIndex[pc]!!) { LinkedList() }.addFirst(label) }
                    val iterator = newMethod.code.listIterator()
                    var unmodifiedIndex = 0
                    while (iterator.hasNext()) {
                        val maybeAdd = instructionsToAdd[unmodifiedIndex++]
                        maybeAdd?.forEach { iterator.add(it) }
                        iterator.next()
                    }
                    break
                }
            }
        }

    }

    fun midClass(classpath: Classpath, klass: Klass) {
        if (this.extending > 0) {
            klass.extending = resolveKlass(classpath, this.extending)
        }
        klass.interfaces.ensureCapacity(this.implementing.size)
        for (interfaceRef in this.implementing) {
            klass.interfaces.add(resolveKlass(classpath, interfaceRef))
        }
        for (attributeHolder in this.fields) {
            val field = Field(klass, JType.fromDescriptor(classpath, CUTF8.assertCUTF8(this.constants[attributeHolder.descriptor]).value), CUTF8.assertCUTF8(this.constants[attributeHolder.name]).value)
            if (attributeHolder.accessFlags and 0x0001 != 0) {
                field.isPublic = true
            }
            if (attributeHolder.accessFlags and 0x0002 != 0) {
                field.isPrivate = true
            }
            if (attributeHolder.accessFlags and 0x0004 != 0) {
                field.isProtected = true
            }
            if (attributeHolder.accessFlags and 0x0008 != 0) {
                field.isStatic = true
            }
            if (attributeHolder.accessFlags and 0x0010 != 0) {
                field.isFinal = true
            }
            if (attributeHolder.accessFlags and 0x0040 != 0) {
                field.isVolatile = true
            }
            if (attributeHolder.accessFlags and 0x0080 != 0) {
                field.isTransient = true
            }
            if (attributeHolder.accessFlags and 0x1000 != 0) {
                field.isSynthetic = true
            }
            if (attributeHolder.accessFlags and 0x4000 != 0) {
                field.isEnum = true
            }
            klass.fields[field.name] = field
        }
        for (attributeHolder in this.methods) {
            val method = Method(klass, CUTF8.assertCUTF8(this.constants[attributeHolder.name]).value, MethodDescriptor.fromString(classpath, CUTF8.assertCUTF8(this.constants[attributeHolder.descriptor]).value)!!)
            if (attributeHolder.accessFlags and 0x0001 != 0) {
                method.isPublic = true
            }
            if (attributeHolder.accessFlags and 0x0002 != 0) {
                method.isPrivate = true
            }
            if (attributeHolder.accessFlags and 0x0004 != 0) {
                method.isProtected = true
            }
            if (attributeHolder.accessFlags and 0x0008 != 0) {
                method.isStatic = true
            }
            if (attributeHolder.accessFlags and 0x0010 != 0) {
                method.isFinal = true
            }
            if (attributeHolder.accessFlags and 0x0020 != 0) {
                method.isSynchronized = true
            }
            if (attributeHolder.accessFlags and 0x0040 != 0) {
                method.isBridge = true
            }
            if (attributeHolder.accessFlags and 0x0080 != 0) {
                method.isVarargs = true
            }
            if (attributeHolder.accessFlags and 0x0100 != 0) {
                method.isNative = true
            }
            if (attributeHolder.accessFlags and 0x0400 != 0) {
                method.isAbstract = true
            }
            if (attributeHolder.accessFlags and 0x0800 != 0) {
                method.isStrict = true
            }
            if (attributeHolder.accessFlags and 0x1000 != 0) {
                method.isSynthetic = true
            }
            klass.methods.add(method)
        }
    }

    fun preClass(): Klass {
        return Klass(majorVersion, minorVersion, CUTF8.assertCUTF8(this.constants[CClass.assertCClass(this.constants[this.name]).value]).value,
                accessFlags and 0x1 != 0, accessFlags and 0x10 != 0, accessFlags and 0x20 != 0, accessFlags and 0x200 != 0, accessFlags and 0x400 != 0, accessFlags and 0x1000 != 0, accessFlags and 0x2000 != 0, accessFlags and 0x4000 != 0, accessFlags and 0x8000 != 0)
    }

}
