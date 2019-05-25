package com.protryon.jasm.stage1

import com.protryon.jasm.*
import com.protryon.jasm.Method
import com.protryon.jasm.stage1.constant.*
import com.shapesecurity.functional.data.Either

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

abstract class CConstant<T> protected constructor(val value: T, val type: Int) {

    open val isDoubled: Boolean
        get() = false

    @Throws(IOException::class)
    abstract fun write(out: DataOutputStream)

    private fun findMethod(klass: Klass, name: String, descriptor: MethodDescriptor): Method? {
        for (method in klass.methods) {
            if (method.name == name && method.descriptor == descriptor) {
                return method
            }
        }
        if (klass.extending != null) {
            val method = findMethod(klass.extending!!, name, descriptor)
            if (method != null) {
                return method
            }
        }
        for (implementing in klass.interfaces) {
            val method = findMethod(implementing, name, descriptor)
            if (method != null) {
                return method
            }
        }
        val method = Method(klass, name, descriptor)
        method.isDummy = true
        klass.methods.add(method)
        return method
    }

    private fun findField(klass: Klass, name: String, type: JType): Field? {
        var field: Field? = klass.fields[name]
        if (field != null) {
            return field
        }
        if (klass.extending != null) {
            field = findField(klass.extending!!, name, type)
            if (field != null) {
                return field
            }
        }
        for (implementing in klass.interfaces) {
            field = findField(implementing, name, type)
            if (field != null) {
                return field
            }
        }
        field = Field(klass, type, name)
        field.isDummy = true
        klass.fields[name] = field
        return field
    }

    fun toConstant(classpath: Classpath, klass: Stage1Class, lookup: (Int)->Constant<*>): Constant<*> {
        val nameAndType: NameAndType
        when (this.type) {
            1 -> return Constant((this as CUTF8).value)
            3 -> return Constant((this as CInteger).value)
            4 -> return Constant((this as CFloat).value)
            5 -> return Constant((this as CLong).value)
            6 -> return Constant((this as CDouble).value)
            7 -> {
                val descriptor = lookup.invoke((this as CClass).value).value as String
                return if (descriptor.startsWith("[")) {
                    Constant(JType.fromDescriptor(classpath, descriptor))
                } else {
                    Constant(JType.instance(classpath.loadKlass(descriptor)))
                }
            }
            8 -> return Constant(lookup.invoke((this as CString).value).value as String)
            9 -> {
                val fieldRef = this as CFieldRef
                val fieldKlassAsType = lookup.invoke(fieldRef.value.left).value as JType
                nameAndType = lookup.invoke(fieldRef.value.right).value as NameAndType
                val fieldKlass = if (fieldKlassAsType is JType.JTypeInstance) fieldKlassAsType.referenceOf() else classpath.loadKlass("java/lang/Object")
                val field = findField(fieldKlass!!, nameAndType.name, nameAndType.type.left().fromJust()) ?: throw RuntimeException("Field not found: $nameAndType")
/*if (!field.type.equals(nameAndType.type.left().fromJust())) {
                    throw new RuntimeException("Type mismatch in field reference");
                }*/
                return Constant(field)
            }
            10 -> {
                val methodRef = this as CMethodRef
                val methodKlassAsType = lookup.invoke(methodRef.value.left).value as JType
                nameAndType = lookup.invoke(methodRef.value.right).value as NameAndType
                val methodKlass = if (methodKlassAsType is JType.JTypeInstance) methodKlassAsType.referenceOf() else classpath.loadKlass("java/lang/Object")
                val method = findMethod(methodKlass!!, nameAndType.name, nameAndType.type.right().fromJust()) ?: throw RuntimeException("Method descriptor not found: $nameAndType")
                return Constant(method)
            }
            11 -> {
                val methodRef = this as CInterfaceMethodRef
                val methodKlassAsType = lookup.invoke(methodRef.value.left).value as JType
                nameAndType = lookup.invoke(methodRef.value.right).value as NameAndType
                val methodKlass = if (methodKlassAsType is JType.JTypeInstance) methodKlassAsType.referenceOf() else classpath.loadKlass("java/lang/Object")
                val method = findMethod(methodKlass!!, nameAndType.name, nameAndType.type.right().fromJust()) ?: throw RuntimeException("Interface method descriptor not found: $nameAndType")
                return Constant(method)
            }
            12 -> {
                val cNameAndType = this as CNameAndType
                val name = lookup.invoke(cNameAndType.value.left).value as String
                val rawDescriptor = lookup.invoke(cNameAndType.value.right).value as String
                return if (rawDescriptor.startsWith("(")) {
                    Constant(NameAndType(name, Either.right(MethodDescriptor.fromString(classpath, rawDescriptor)!!)))
                } else Constant(NameAndType(name, Either.left(JType.fromDescriptor(classpath, rawDescriptor))))
            }
            15 -> {
                val handle = this as CMethodHandle
                return Constant(MethodHandle(lookup.invoke(handle.value.right), MethodHandle.MethodHandleType.values()[handle.value.left.toInt()]))
            }
            16 -> {
                val methodType = this as CMethodType
                return Constant(MethodDescriptor.fromString(classpath, lookup.invoke(methodType.value).value as String))
            }
            17, 18 -> {
                val invokeDynamic = this as CInvokeDynamic
                val bootstrapMethod = klass.bootstrapMethods[invokeDynamic.value.left]!!
                val methodHandle = lookup.invoke(bootstrapMethod.methodRef)
                val arguments = Array(bootstrapMethod.arguments.size) {
                    lookup.invoke(bootstrapMethod.arguments[it])
                }
                val newBootstrapMethod = BootstrapMethod(methodHandle.value as MethodHandle, arguments)
                val bootstrapNameAndType = lookup.invoke(invokeDynamic.value.right)
                return Constant(Dynamic(this.type == 18, newBootstrapMethod, bootstrapNameAndType.value as NameAndType))
            }
            19 -> throw UnsupportedOperationException("CModule")
            //return new CModule(in.readUnsignedShort());
            20 -> throw UnsupportedOperationException("CPackage")
            //return new CPackage(in.readUnsignedShort());
            else -> throw UnsupportedOperationException("Invalid constant type: $type")
        }
    }

    companion object {

        @Throws(IOException::class)
        fun read(inputStream: DataInputStream): CConstant<*> {
            val type = inputStream.read()
            when (type) {
                1 -> {
                    val utf = ByteArray(inputStream.readUnsignedShort())
                    inputStream.readFully(utf)
                    return CUTF8(String(utf))
                }
                3 -> return CInteger(inputStream.readInt())
                4 -> return CFloat(inputStream.readFloat())
                5 -> return CLong(inputStream.readLong())
                6 -> return CDouble(inputStream.readDouble())
                7 -> return CClass(inputStream.readUnsignedShort())
                8 -> return CString(inputStream.readUnsignedShort())
                9 -> return CFieldRef(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                10 -> return CMethodRef(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                11 -> return CInterfaceMethodRef(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                12 -> return CNameAndType(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                15 -> return CMethodHandle(inputStream.read().toByte(), inputStream.readUnsignedShort())
                16 -> return CMethodType(inputStream.readUnsignedShort())
                17 -> return CDynamic(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                18 -> return CInvokeDynamic(inputStream.readUnsignedShort(), inputStream.readUnsignedShort())
                19 -> return CModule(inputStream.readUnsignedShort())
                20 -> return CPackage(inputStream.readUnsignedShort())
                else -> throw RuntimeException("Invalid constant type: $type")
            }
        }
    }
}
