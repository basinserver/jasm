package com.protryon.jasm

import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.NameExpr
import com.protryon.jasm.decompiler.StackEntry

class Local @JvmOverloads constructor(val index: Int, val type: JType? = null) {

    fun resetType(type: JType): Local {
        return if (this.type == null || !type.assignableTo(this.type)) {
            Local(index, type)
        } else this
    }

    fun stackify(): StackEntry<Expression> {
        return StackEntry(this.type!!, NameExpr(this.toString()))
    }

    override fun toString(): String {
        return "v" + this.index
    }

}
