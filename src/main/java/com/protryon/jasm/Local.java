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

    public void setOrAssertType(JType type) {
        if (this.type == null) {
            this.type = type;
        }
        this.type.assertType(type);
    }

}
