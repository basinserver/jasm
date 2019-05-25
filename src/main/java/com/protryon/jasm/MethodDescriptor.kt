package com.protryon.jasm

import com.shapesecurity.functional.Pair

import java.util.ArrayList

class MethodDescriptor(var returnType: JType, var parameters: ArrayList<JType>) {

    override fun toString(): String {
        val builder = StringBuilder("(")
        for (param in parameters) {
            builder.append(param.toDescriptor())
        }
        builder.append(")")
        builder.append(returnType.toDescriptor())
        return builder.toString()
    }

    fun niceString(methodName: String): String {
        val builder = StringBuilder()
        builder.append(returnType.niceName)
        builder.append(" ").append(methodName).append("(")
        var first = true
        for (param in parameters) {
            if (first) {
                first = false
            } else {
                builder.append(", ")
            }
            builder.append(param.niceName)
        }
        builder.append(")")
        return builder.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (o !is MethodDescriptor) {
            return false
        }
        if (o.returnType != this.returnType) {
            return false
        }
        if (o.parameters.size != this.parameters.size) {
            return false
        }
        for (i in this.parameters.indices) {
            if (this.parameters[i] != o.parameters[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return this.returnType.hashCode() + this.parameters.hashCode()
    }

    companion object {

        fun fromString(classpath: Classpath, str: String): MethodDescriptor? {
            if (str[0] != '(') {
                return null
            }
            val end = str.indexOf(")", 1)
            var params = str.substring(1, end)
            val returnDescriptor = str.substring(end + 1)
            val returnType = JType.fromDescriptor(classpath, returnDescriptor)
            val parameters = ArrayList<JType>()
            while (params.length > 0) {
                val pair = JType.fromDescriptorWithLength(classpath, params)
                params = params.substring(pair.right)
                parameters.add(pair.left)
            }
            return MethodDescriptor(returnType, parameters)
        }
    }

}
