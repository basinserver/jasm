package com.protryon.jasm.decompiler;

public class StackEntry<T> {
    public final StackEntryType type;
    public final T value;

    public StackEntry(StackEntryType type, T value) {
        this.type = type;
        this.value = value;
    }

}
