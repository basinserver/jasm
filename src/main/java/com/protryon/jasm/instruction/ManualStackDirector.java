package com.protryon.jasm.instruction;

import com.google.common.collect.Lists;
import com.protryon.jasm.Constant;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class ManualStackDirector {

    private ManualStackDirector() {

    }

    protected static <T> void reduceInstruction(StackReducer<T> reducer, Instruction instruction, LinkedList<T> stack) {
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
                List<T> arguments = new ArrayList<>();
                for (int i = 0; i < method.descriptor.parameters.size(); ++i) {
                    arguments.add(stack.pop());
                }
                Lists.reverse(arguments);
                T objectref = stack.pop();
                reducer.reduceInvokeinterface((Invokeinterface) instruction, objectref, arguments).foreach(stack::push);
                break;
            }
            case 184: { // invokestatic
                Method method = ((Constant<Method>) ((Invokestatic) instruction).indexbyte).value;
                List<T> arguments = new ArrayList<>();
                for (int i = 0; i < method.descriptor.parameters.size(); ++i) {
                    arguments.add(stack.pop());
                }
                Lists.reverse(arguments);
                reducer.reduceInvokestatic((Invokestatic) instruction, arguments).foreach(stack::push);
                break;
            }
            case 182: { // invokevirtual
                Method method = ((Constant<Method>) ((Invokevirtual) instruction).indexbyte).value;
                List<T> arguments = new ArrayList<>();
                for (int i = 0; i < method.descriptor.parameters.size(); ++i) {
                    arguments.add(stack.pop());
                }
                Lists.reverse(arguments);
                T objectref = stack.pop();
                reducer.reduceInvokevirtual((Invokevirtual) instruction, objectref, arguments).foreach(stack::push);
                break;
            }
            case 183: { // invokespecial
                Method method = ((Constant<Method>) ((Invokespecial) instruction).indexbyte).value;
                List<T> arguments = new ArrayList<>();
                for (int i = 0; i < method.descriptor.parameters.size(); ++i) {
                    arguments.add(stack.pop());
                }
                Lists.reverse(arguments);
                T objectref = stack.pop();
                reducer.reduceInvokespecial((Invokespecial) instruction, objectref, arguments).foreach(stack::push);
                break;
            }
            case 186: { // invokedynamic
                throw new UnsupportedOperationException("invokedynamic in reducer");
            }
            case 171: { // lookupswitch
                T key = stack.pop();
                reducer.reduceLookupswitch((Lookupswitch) instruction, key);
                break;
            }
            case 170: { // tableswitch
                T key = stack.pop();
                reducer.reduceTableswitch((Tableswitch) instruction, key);
                break;
            }
            case 191: { // athrow
                T objectref = stack.pop();
                T pushed = reducer.reduceAthrow((Athrow) instruction, objectref);
                throw new UnsupportedOperationException("TODO: find catch block for exception type then modify stack at that point");
                // break;
            }
            case 197: { // multianewarray
                Multianewarray multianewarray = (Multianewarray) instruction;
                List<T> dimensions = new ArrayList<>(multianewarray.dimensions);
                for (int i = 0; i < dimensions.size(); ++i) {
                    dimensions.add(stack.pop());
                }
                Lists.reverse(dimensions);
                T pushed = reducer.reduceMultianewarray(multianewarray, dimensions);
                stack.push(pushed);
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
    }
}
