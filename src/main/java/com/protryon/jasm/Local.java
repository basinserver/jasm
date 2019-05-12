package com.protryon.jasm;

public final class Local {

    public final Method method;
    public JType type;
    public final int index;

    public Local(Method method, int index) {
        this.method = method;
        this.type = null;
        this.index = index;
    }

    public JType resetType(JType type) {
        if (this.type == null || !type.assignableTo(this.type)) {
            this.type = type;
        }
        return this.type;
    }

}
