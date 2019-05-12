package com.protryon.jasm.instruction;

import com.protryon.jasm.Constant;
import com.protryon.jasm.Dynamic;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;

public final class ManualStackDirector {

    private ManualStackDirector() {

    }

    protected static <T> ImmutableList<T> reduceInstruction(StackReducer<T> reducer, Instruction instruction, ImmutableList<T> stack) {
        switch (instruction.opcode()) {
            case 88: {
                throw new UnsupportedOperationException("pop2");
                // reducer.reducePop2((Pop2) i);

                // break;
            }
            case 91: {
                throw new UnsupportedOperationException("dup_x2");
                // reducer.reduceDup_x2((Dup_x2) i);

                // break;
            }
            case 92: {
                throw new UnsupportedOperationException("dup2");
                // reducer.reduceDup2((Dup2) i);

                // break;
            }
            case 93: {
                throw new UnsupportedOperationException("dup2_x1");
                // reducer.reduceDup2_x1((Dup2_x1) i);

                // break;
            }
            case 94: {
                throw new UnsupportedOperationException("dup2_x2");
                // reducer.reduceDup2_x2((Dup2_x2) i);

                // break;
            }
            case 185: { // invokeinterface
                Method method = ((Constant<Method>) ((Invokeinterface) instruction).indexbyte).value;
                ImmutableList<T> arguments = stack.take(method.descriptor.parameters.size()).reverse();
                stack = stack.drop(method.descriptor.parameters.size());
                T objectref = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                Maybe<T> returnValue = reducer.reduceInvokeinterface((Invokeinterface) instruction, objectref, arguments.toArrayList());
                if (returnValue.isJust()) {
                    stack = stack.cons(returnValue.fromJust());
                }
                break;
            }
            case 184: { // invokestatic
                Method method = ((Constant<Method>) ((Invokestatic) instruction).indexbyte).value;
                ImmutableList<T> arguments = stack.take(method.descriptor.parameters.size()).reverse();
                stack = stack.drop(method.descriptor.parameters.size());
                Maybe<T> returnValue = reducer.reduceInvokestatic((Invokestatic) instruction, arguments.toArrayList());
                if (returnValue.isJust()) {
                    stack = stack.cons(returnValue.fromJust());
                }
                break;
            }
            case 182: { // invokevirtual
                Method method = ((Constant<Method>) ((Invokevirtual) instruction).indexbyte).value;
                ImmutableList<T> arguments = stack.take(method.descriptor.parameters.size()).reverse();
                stack = stack.drop(method.descriptor.parameters.size());
                T objectref = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                Maybe<T> returnValue = reducer.reduceInvokevirtual((Invokevirtual) instruction, objectref, arguments.toArrayList());
                if (returnValue.isJust()) {
                    stack = stack.cons(returnValue.fromJust());
                }
                break;
            }
            case 183: { // invokespecial
                Method method = ((Constant<Method>) ((Invokespecial) instruction).indexbyte).value;
                ImmutableList<T> arguments = stack.take(method.descriptor.parameters.size()).reverse();
                stack = stack.drop(method.descriptor.parameters.size());
                T objectref = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                Maybe<T> returnValue = reducer.reduceInvokespecial((Invokespecial) instruction, objectref, arguments.toArrayList());
                if (returnValue.isJust()) {
                    stack = stack.cons(returnValue.fromJust());
                }
                break;
            }
            case 186: { // invokedynamic
                Dynamic dynamic = ((Constant<Dynamic>) ((Invokedynamic) instruction).indexbyte).value;
                ImmutableList<T> arguments = stack.take(dynamic.nameAndType.type.right().fromJust().parameters.size()).reverse();
                stack = stack.drop(dynamic.nameAndType.type.right().fromJust().parameters.size());
                Maybe<T> returnValue = reducer.reduceInvokedynamic((Invokedynamic) instruction, arguments.toArrayList());
                if (returnValue.isJust()) {
                    stack = stack.cons(returnValue.fromJust());
                }
                break;
            }
            case 171: { // lookupswitch
                T key = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                reducer.reduceLookupswitch((Lookupswitch) instruction, key);
                break;
            }
            case 170: { // tableswitch
                T key = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                reducer.reduceTableswitch((Tableswitch) instruction, key);
                break;
            }
            case 191: { // athrow
                T objectref = stack.maybeHead().fromJust();
                stack = stack.maybeTail().fromJust();
                T pushed = reducer.reduceAthrow((Athrow) instruction, objectref);
                stack = stack.cons(pushed);
                break;
            }
            case 197: { // multianewarray
                Multianewarray multianewarray = (Multianewarray) instruction;
                ImmutableList<T> dimensions = stack.take(multianewarray.dimensions).reverse();
                stack = stack.drop(multianewarray.dimensions);
                T pushed = reducer.reduceMultianewarray(multianewarray, dimensions.toArrayList());
                stack = stack.cons(pushed);
                break;
            }

            // wide cannot occur in instructions (eaten during decoding)
            case -1: { // EnterTry psuedo instruction
                reducer.reduceEnterTry((EnterTry) instruction);
                break;
            }
            case -2: { // ExitTry psuedo instruction
                reducer.reduceExitTry((ExitTry) instruction);
                break;
            }
            case -3: { // Label psuedo instruction
                reducer.reduceLabel((Label) instruction);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown opcode in reducer: " + instruction.opcode());
        }
        return stack;
    }
}
