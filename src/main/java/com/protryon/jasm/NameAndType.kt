package com.protryon.jasm

import com.shapesecurity.functional.data.Either

class NameAndType(val name: String, val type: Either<JType, MethodDescriptor>) {

    override fun toString(): String {
        return this.name + " " + type.either({ it.toString() }, { it.toString() })
    }

}
