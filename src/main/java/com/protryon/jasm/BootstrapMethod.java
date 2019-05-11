package com.protryon.jasm;

public class BootstrapMethod {

    public final MethodHandle methodRef;
    public final Constant[] arguments;

    public BootstrapMethod(MethodHandle methodRef, Constant[] arguments) {
        this.methodRef = methodRef;
        this.arguments = arguments;
    }
}
