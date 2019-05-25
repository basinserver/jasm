package com.protryon.jasm

import java.util.ArrayList
import java.util.LinkedHashMap

class Klass {

    var majorVersion: Int = 0
    var minorVersion: Int = 0
    var name: String
    var extending: Klass? = null
    var interfaces = ArrayList<Klass>()
    var fields = LinkedHashMap<String, Field>()
    var methods = ArrayList<Method>()

    var isPublic = false
    var isFinal = false
    var isSuper = false
    var isInterface = false
    var isAbstract = false
    var isSynthetic = false
    var isAnnotation = false
    var isEnum = false
    var isModule = false


    constructor(majorVersion: Int, minorVersion: Int, name: String) {
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
        this.name = name
    }

    constructor(majorVersion: Int, minorVersion: Int, name: String, isPublic: Boolean, isFinal: Boolean, isSuper: Boolean, isInterface: Boolean, isAbstract: Boolean, isSynthetic: Boolean, isAnnotation: Boolean, isEnum: Boolean, isModule: Boolean) {
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
        this.name = name
        this.isPublic = isPublic
        this.isFinal = isFinal
        this.isSuper = isSuper
        this.isInterface = isInterface
        this.isAbstract = isAbstract
        this.isSynthetic = isSynthetic
        this.isAnnotation = isAnnotation
        this.isEnum = isEnum
        this.isModule = isModule
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("class name: ").append(name).append("\n")
        if (extending != null) {
            sb.append("extending: ").append(extending!!.name).append("\n")
        }
        if (interfaces.size > 0) {
            sb.append("implementing: ")
            for (klass in interfaces) {
                sb.append(klass.name).append(", ")
            }
            sb.append("\n")
        }
        fields.forEach { name, field ->
            if (field.isStatic) {
                sb.append("static ")
            }
            sb.append(field.type.niceName)
            sb.append(" ").append(field.name)
            sb.append(";\n\n")
        }
        sb.append("\n\n\n")
        for (method in methods) {
            sb.append(method.toString())
        }
        return sb.toString()
    }
}
