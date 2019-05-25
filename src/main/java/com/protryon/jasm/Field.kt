package com.protryon.jasm

class Field(val parent: Klass, var type: JType, var name: String) {
    var isPublic = false
    var isPrivate = false
    var isProtected = false
    var isStatic = false
    var isFinal = false
    var isVolatile = false
    var isTransient = false
    var isSynthetic = false
    var isEnum = false
    var constantValue: Constant<*>? = null

    // set for created methods in things like stdlib or unincluded libs
    var isDummy = false

}
