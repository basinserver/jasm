package com.protryon.jasm.decompiler;

import com.protryon.jasm.JType;

public class StackEntry<T> {
    public final JType type;
    public final T value;

    public StackEntry(JType type, T value) {
        this.type = type;
        this.value = value;
    }

}
