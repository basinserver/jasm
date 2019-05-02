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
            case -2: { // EnterTry psuedo instruction
                reducer.reduceExitTry((ExitTry) instruction);
                break;
            }
            case -3: { // EnterTry psuedo instruction
                reducer.reduceLabel((Label) instruction);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown opcode in reducer: " + instruction.opcode());
        }
    }
}
