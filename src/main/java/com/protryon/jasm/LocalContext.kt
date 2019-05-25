package com.protryon.jasm

import com.shapesecurity.functional.data.HashTable
import com.shapesecurity.functional.data.Maybe

class LocalContext {

    var locals = HashTable.emptyUsingEquality<Int, Local>()

    fun getOrMakeLocal(index: Int): Local {
        val localMaybe = locals.get(index)
        if (localMaybe.isJust) {
            return localMaybe.fromJust()
        }
        val newLocal = Local(index)
        this.locals = this.locals.put(index, newLocal)
        return newLocal
    }

    fun getOrMakeLocal(index: Int, type: JType?): Local {
        val localMaybe = locals.get(index)
        if (localMaybe.isJust) {
            return if (type != null && (localMaybe.fromJust().type == null || !type.assignableTo(localMaybe.fromJust().type))) {
                this.updateLocal(localMaybe.fromJust().resetType(type))
            } else localMaybe.fromJust()
        }
        val newLocal = Local(index, type)
        this.locals = this.locals.put(index, newLocal)
        return newLocal
    }

    fun updateLocal(local: Local): Local {
        this.locals = this.locals.put(local.index, local)
        return local
    }

    fun fork(): LocalContext {
        val localContext = LocalContext()
        localContext.locals = this.locals
        return localContext
    }

}
