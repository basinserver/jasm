package com.protryon.jasm.stage1;

public class CBootstrapMethod {

    public final int methodRef;
    public final int[] arguments;

    public CBootstrapMethod(int methodRef, int[] arguments) {
        this.methodRef = methodRef;
        this.arguments = arguments;
    }
}
