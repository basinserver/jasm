package com.protryon.jasm.instruction;

import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.data.Maybe;

import java.util.List;

abstract class ManualStackReducer<T> {

    public abstract Maybe<T> reduceInvokeinterface(Invokeinterface instruction, T objectref, List<T> arguments);

    public abstract Maybe<T> reduceInvokestatic(Invokestatic instruction, List<T> arguments);

    public abstract Maybe<T> reduceInvokevirtual(Invokevirtual instruction, T objectref, List<T> arguments);

    public final Maybe<T> reduceInvokedynamic(Invokedynamic instruction) {
        throw new UnsupportedOperationException("Invokedynamic reduction");
    }

    public abstract Maybe<T> reduceInvokespecial(Invokespecial instruction, T objectref, List<T> arguments);

    public abstract T reduceMultianewarray(Multianewarray instruction, List<T> count);

    public abstract void reduceLookupswitch(Lookupswitch instruction, T index);

    public abstract void reduceTableswitch(Tableswitch instruction, T index);

    public abstract void reduceEnterTry(EnterTry instruction);

    public abstract void reduceExitTry(ExitTry instruction);

    public abstract void reduceLabel(Label instruction);
}
