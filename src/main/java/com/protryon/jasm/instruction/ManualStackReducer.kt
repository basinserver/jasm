package com.protryon.jasm.instruction

import com.protryon.jasm.instruction.instructions.*
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.data.ImmutableList

abstract class ManualStackReducer<T> {

    abstract fun reduceInvokeinterface(instruction: Invokeinterface, objectref: T, arguments: List<T>): T?

    abstract fun reduceInvokestatic(instruction: Invokestatic, arguments: List<T>): T?

    abstract fun reduceInvokevirtual(instruction: Invokevirtual, objectref: T, arguments: List<T>): T?

    abstract fun reduceInvokedynamic(instruction: Invokedynamic, arguments: List<T>): T?

    abstract fun reduceInvokespecial(instruction: Invokespecial, objectref: T, arguments: List<T>): T?

    abstract fun reduceMultianewarray(instruction: Multianewarray, count: List<T>): T

    abstract fun reduceLookupswitch(instruction: Lookupswitch, index: T)

    abstract fun reduceTableswitch(instruction: Tableswitch, index: T)

    abstract fun reduceEnterTry(instruction: EnterTry)

    abstract fun reduceExitTry(instruction: ExitTry)

    abstract fun reduceLabel(instruction: Label)

    abstract fun reduceAthrow(instruction: Athrow, objectref: T): T

    // length: 2 -> 3, 3 -> 4
    abstract fun reduceDup_x2(instruction: Dup_x2, value1: T, value2: T, value3: T?): ImmutableList<T>

    // length: 2 -> 4, 1 -> 2
    abstract fun reduceDup2(instruction: Dup2, value1: T, value2: T?): ImmutableList<T>

    // must be length 5 if value3.isJust else 3
    abstract fun reduceDup2_x1(instruction: Dup2_x1, value1: T, value2: T, value3: T?): ImmutableList<T>

    // length: 4 -> 6, 3 -> 4 || 5, 2 -> 3
    abstract fun reduceDup2_x2(instruction: Dup2_x2, value1: T, value2: T, value3: T?, value4: T?): ImmutableList<T>

    abstract fun reducePop2(instruction: Pop2, value1: T, value2: T?)
}
