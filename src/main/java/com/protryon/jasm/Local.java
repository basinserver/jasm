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

    public JType setOrAssertType(JType type) {
        if (this.type == null) {
            this.type = type;
        }
        this.type.assertType(type);
        return this.type;
    }

    public JType setOrAssertTypes(JType... types) {
        if (this.type == null) {
            this.type = types[0];
            return this.type;
        }
        this.type.assertTypes(types);
        return this.type;
    }

}
