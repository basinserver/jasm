package com.protryon.jasm.instruction

import com.protryon.jasm.Constant
import com.protryon.jasm.Dynamic
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.instructions.*
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry
import com.protryon.jasm.instruction.psuedoinstructions.Label
import com.shapesecurity.functional.F
import com.shapesecurity.functional.data.ImmutableList
import com.shapesecurity.functional.data.Maybe

object ManualStackDirector {

    internal fun <T> reduceInstruction(reducer: StackReducer<T>, instruction: Instruction, stack: ImmutableList<T>, isDoubled: (T)->Boolean): ImmutableList<T> {
        var stack = stack
        when (instruction.opcode()) {
            88 -> {
                val popped = stack.maybeHead().fromJust()
                var secondOption: T?
                if (isDoubled.invoke(popped)) {
                    secondOption = null
                    stack = stack.maybeTail().fromJust()
                } else {
                    secondOption = stack.index(1).toNullable()
                    stack = stack.drop(2)
                }
                reducer.reducePop2(instruction as Pop2, popped, secondOption)
            }
            91 -> {
                throw UnsupportedOperationException("dup_x2")
                // reducer.reduceDup_x2((Dup_x2) i);

                // break;
            }
            92 -> {
                throw UnsupportedOperationException("dup2")
                // reducer.reduceDup2((Dup2) i);

                // break;
            }
            93 -> {
                throw UnsupportedOperationException("dup2_x1")
                // reducer.reduceDup2_x1((Dup2_x1) i);

                // break;
            }
            94 -> {
                throw UnsupportedOperationException("dup2_x2")
                // reducer.reduceDup2_x2((Dup2_x2) i);

                // break;
            }
            185 -> { // invokeinterface
                val method = ((instruction as Invokeinterface).indexbyte as Constant<Method>).value
                val arguments = stack.take(method.descriptor.parameters.size).reverse()
                stack = stack.drop(method.descriptor.parameters.size)
                val objectref = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                val returnValue = reducer.reduceInvokeinterface(instruction, objectref, arguments.toArrayList())
                if (returnValue != null) {
                    stack = stack.cons(returnValue)
                }
            }
            184 -> { // invokestatic
                val method = ((instruction as Invokestatic).indexbyte as Constant<Method>).value
                val arguments = stack.take(method.descriptor.parameters.size).reverse()
                stack = stack.drop(method.descriptor.parameters.size)
                val returnValue = reducer.reduceInvokestatic(instruction, arguments.toArrayList())
                if (returnValue != null) {
                    stack = stack.cons(returnValue)
                }
            }
            182 -> { // invokevirtual
                val method = ((instruction as Invokevirtual).indexbyte as Constant<Method>).value
                val arguments = stack.take(method.descriptor.parameters.size).reverse()
                stack = stack.drop(method.descriptor.parameters.size)
                val objectref = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                val returnValue = reducer.reduceInvokevirtual(instruction, objectref, arguments.toArrayList())
                if (returnValue != null) {
                    stack = stack.cons(returnValue)
                }
            }
            183 -> { // invokespecial
                val method = ((instruction as Invokespecial).indexbyte as Constant<Method>).value
                val arguments = stack.take(method.descriptor.parameters.size).reverse()
                stack = stack.drop(method.descriptor.parameters.size)
                val objectref = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                val returnValue = reducer.reduceInvokespecial(instruction, objectref, arguments.toArrayList())
                if (returnValue != null) {
                    stack = stack.cons(returnValue)
                }
            }
            186 -> { // invokedynamic
                val dynamic = ((instruction as Invokedynamic).indexbyte as Constant<Dynamic>).value
                val arguments = stack.take(dynamic.nameAndType.type.right().fromJust().parameters.size).reverse()
                stack = stack.drop(dynamic.nameAndType.type.right().fromJust().parameters.size)
                val returnValue = reducer.reduceInvokedynamic(instruction, arguments.toArrayList())
                if (returnValue != null) {
                    stack = stack.cons(returnValue)
                }
            }
            171 -> { // lookupswitch
                val key = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                reducer.reduceLookupswitch(instruction as Lookupswitch, key)
            }
            170 -> { // tableswitch
                val key = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                reducer.reduceTableswitch(instruction as Tableswitch, key)
            }
            191 -> { // athrow
                val objectref = stack.maybeHead().fromJust()
                stack = stack.maybeTail().fromJust()
                val pushed = reducer.reduceAthrow(instruction as Athrow, objectref)
                stack = stack.cons(pushed)
            }
            197 -> { // multianewarray
                val multianewarray = instruction as Multianewarray
                val dimensions = stack.take(multianewarray.dimensions!!).reverse()
                stack = stack.drop(multianewarray.dimensions!!)
                val pushed = reducer.reduceMultianewarray(multianewarray, dimensions.toArrayList())
                stack = stack.cons(pushed)
            }

            // wide cannot occur in instructions (eaten during decoding)
            -1 -> { // EnterTry psuedo instruction
                reducer.reduceEnterTry(instruction as EnterTry)
            }
            -2 -> { // ExitTry psuedo instruction
                reducer.reduceExitTry(instruction as ExitTry)
            }
            -3 -> { // Label psuedo instruction
                reducer.reduceLabel(instruction as Label)
            }
            else -> throw UnsupportedOperationException("Unknown opcode in reducer: " + instruction.opcode())
        }
        return stack
    }
}
