package com.protryon.jasm

import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.instruction.psuedoinstructions.CatchLabel
import com.protryon.jasm.instruction.psuedoinstructions.Label

import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.LinkedList

class Method(val parent: Klass, var name: String, var descriptor: MethodDescriptor) {
    var isPublic = false
    var isPrivate = false
    var isProtected = false
    var isStatic = false
    var isFinal = false
    var isSynchronized = false
    var isBridge = false
    var isVarargs = false
    var isNative = false
    var isAbstract = false
    var isStrict = false
    var isSynthetic = false

    // set for created methods in things like stdlib or unincluded libs
    var isDummy = false

    var tempVariableCounter = 0

    var code = LinkedList<Instruction>()
    var labels = LinkedHashMap<String, Label>()

    fun getOrMakeLabel(name: String): Label {
        if (labels.containsKey(name)) {
            return labels[name]!!
        }
        val label = Label(name)
        labels[name] = label
        return label
    }

    fun makeCatch(name: String): CatchLabel {
        if (labels.containsKey(name)) {
            return labels[name] as CatchLabel
        }
        val label = CatchLabel(name)
        labels[name] = label
        return label
    }

    override fun toString(): String {
        val sb = StringBuilder()
        if (this.isPublic) {
            sb.append("public ")
        }
        if (this.isPrivate) {
            sb.append("private ")
        }
        if (this.isProtected) {
            sb.append("protected ")
        }
        if (this.isStatic) {
            sb.append("static ")
        }
        if (this.isFinal) {
            sb.append("final ")
        }
        if (this.isSynchronized) {
            sb.append("synchronized ")
        }
        if (this.isNative) {
            sb.append("native ")
        }
        if (this.isAbstract) {
            sb.append("abstract ")
        }
        sb.append(this.descriptor.niceString(this.name)).append(" {\n")
        for (instruction in this.code) {
            sb.append("  ").append(instruction.toString()).append("\n")
        }
        sb.append("}\n")
        return sb.toString()
    }

}
