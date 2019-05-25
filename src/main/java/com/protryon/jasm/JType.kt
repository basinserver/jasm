package com.protryon.jasm

import com.shapesecurity.functional.Pair

import java.util.Arrays
import java.util.Stack

open class JType private constructor(val niceName: String, val javaName: String, val computationType: Int) {

    class JTypeInstance internal constructor(val klass: Klass) : JType(klass.name, "L" + klass.name + ";", 1)

    class JTypeArray internal constructor(val elementType: JType) : JType(elementType.niceName + "[]", "[" + elementType.javaName, 1)

    override fun toString(): String {
        return this.niceName
    }

    fun toDescriptor(): String {
        return this.javaName
    }

    override fun equals(o: Any?): Boolean {
        return o is JType && o.javaName == this.javaName
    }

    fun assignableTo(o: Any?): Boolean {
        if (this === JType.nullT && (o is JTypeInstance || o is JTypeArray)) {
            return true
        }
        if (o !is JTypeInstance && this !is JTypeInstance && o !is JTypeArray && this !is JTypeArray) {
            return true
        }
        if (o !is JTypeInstance || this !is JTypeInstance) {
            return this == o
        }
        val other = o as JTypeInstance?
        if (other!!.javaName == this.javaName) {
            return true
        }
        val assigningKlass = this.klass
        val currentKlass = other.klass
        val awaitingKlass = Stack<Klass>()
        awaitingKlass.push(assigningKlass)
        while (!awaitingKlass.isEmpty()) {
            val klass = awaitingKlass.pop()
            if (currentKlass == klass) {
                return true
            }
            if (klass.extending != null) {
                awaitingKlass.push(klass.extending)
            }
            klass.interfaces.forEach {
                awaitingKlass.push(it)
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return this.javaName.hashCode() + 1
    }

    fun elementType(): JType {
        if (this === JType.nullT) {
            return JType.nullT
        }
        if (this !is JTypeArray) {
            throw RuntimeException("Illegal non-array when expecting array type: $this")
        }
        return this.elementType
    }

    fun referenceOf(): Klass? {
        if (this === nullT) {
            return null
        }
        if (this !is JTypeInstance) {
            throw RuntimeException("Illegal non-instance when expecting instance type: $this")
        }
        return this.klass
    }

    fun assertReference() {
        if (this !== nullT && this !is JTypeInstance && this !is JTypeArray) {
            throw RuntimeException("Illegal non-instance when expecting instance type: $this")
        }
    }

    fun assertType(other: JType?) {
        if (this != other) {
            throw RuntimeException("Got type \"" + this.niceName + "\" when expecting type \"" + (other?.niceName ?: "NULL") + "\"")
        }
    }

    fun assertAssignableTo(to: JType?) {
        if (!this.assignableTo(to)) {
            throw RuntimeException("Got type \"" + this.niceName + "\" when expecting type \"" + (to?.niceName ?: "NULL") + "\"")
        }
    }

    fun isTypes(vararg others: JType): Boolean {
        return Arrays.asList(*others).contains(this)
    }

    fun assertTypes(vararg others: JType) {
        if (!isTypes(*others)) {
            throw RuntimeException("Got type \"" + this.niceName + "\" when expecting type other types.")
        }
    }

    fun assertComputationType(type: Int) {
        if (this.computationType != type) {
            throw RuntimeException("Got type \"" + this.niceName + "\" when expecting computation type \"" + type + "\"")
        }
    }

    companion object {

        val voidT = JType("void", "V", -1)
        val byteT = JType("int", "B", 1)
        val charT = JType("char", "C", 1)
        val shortT = JType("short", "S", 1)
        val intT = JType("int", "I", 1)
        val longT = JType("long", "J", 2)
        val floatT = JType("float", "F", 1)
        val doubleT = JType("double", "D", 2)
        val booleanT = JType("boolean", "Z", 1)
        val nullT = JType("null", "N", -1) // internal, special case for general, untyped null

        fun instance(klass: Klass): JType {
            return JTypeInstance(klass)
        }

        fun array(type: JType): JType {
            return JTypeArray(type)
        }

        fun fromDescriptor(classpath: Classpath, descriptor: String): JType {
            return fromDescriptorWithLength(classpath, descriptor).left
        }

        fun fromDescriptorWithLength(classpath: Classpath, descriptor: String): Pair<JType, Int> {
            when (descriptor[0]) {
                'V' -> return Pair.of(voidT, 1)
                'B' -> return Pair.of(byteT, 1)
                'C' -> return Pair.of(charT, 1)
                'S' -> return Pair.of(shortT, 1)
                'I' -> return Pair.of(intT, 1)
                'J' -> return Pair.of(longT, 1)
                'F' -> return Pair.of(floatT, 1)
                'D' -> return Pair.of(doubleT, 1)
                'Z' -> return Pair.of(booleanT, 1)
                '[' -> {
                    val pair = fromDescriptorWithLength(classpath, descriptor.substring(1))
                    return pair.mapLeft { array(it) }.mapRight { x -> x + 1 }
                }
                'L' -> {
                    val semiIndex = descriptor.indexOf(";", 1)
                    val className = descriptor.substring(1, semiIndex)
                    return Pair.of(JTypeInstance(classpath.loadKlass(className)), className.length + 2)
                }
                else -> throw RuntimeException("Invalid descriptor: " + descriptor[0])
            }
        }
    }
}
