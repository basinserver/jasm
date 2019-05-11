package com.protryon.jasm.instruction;

import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.Tuple4;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;

import java.util.List;

abstract class ManualStackReducer<T> {

    public abstract Maybe<T> reduceInvokeinterface(Invokeinterface instruction, T objectref, List<T> arguments);

    public abstract Maybe<T> reduceInvokestatic(Invokestatic instruction, List<T> arguments);

    public abstract Maybe<T> reduceInvokevirtual(Invokevirtual instruction, T objectref, List<T> arguments);

    public abstract Maybe<T> reduceInvokedynamic(Invokedynamic instruction, List<T> arguments);

    public abstract Maybe<T> reduceInvokespecial(Invokespecial instruction, T objectref, List<T> arguments);

    public abstract T reduceMultianewarray(Multianewarray instruction, List<T> count);

    public abstract void reduceLookupswitch(Lookupswitch instruction, T index);

    public abstract void reduceTableswitch(Tableswitch instruction, T index);

    public abstract void reduceEnterTry(EnterTry instruction);

    public abstract void reduceExitTry(ExitTry instruction);

    public abstract void reduceLabel(Label instruction);

    public abstract T reduceAthrow(Athrow instruction, T objectref);

    // length: 2 -> 3, 3 -> 4
    public abstract ImmutableList<T> reduceDup_x2(Dup_x2 instruction, T value1, T value2, Maybe<T> value3);

    // length: 2 -> 4, 1 -> 2
    public abstract ImmutableList<T> reduceDup2(Dup2 instruction, T value1, Maybe<T> value2);

    // must be length 5 if value3.isJust else 3
    public abstract ImmutableList<T> reduceDup2_x1(Dup2_x1 instruction, T value1, T value2, Maybe<T> value3);

    // length: 4 -> 6, 3 -> 4 || 5, 2 -> 3
    public abstract ImmutableList<T> reduceDup2_x2(Dup2_x2 instruction, T value1, T value2, Maybe<T> value3, Maybe<T> value4);

    public abstract void reducePop2(Pop2 instruction, T value1, Maybe<T> value2);
}
