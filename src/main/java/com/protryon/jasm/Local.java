package com.protryon.jasm;

public final class Local {

    public final Method method;
    public final int index;

    public Local(Method method, int index) {
        this.method = method;
        this.index = index;
    }

}
