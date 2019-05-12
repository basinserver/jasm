package com.protryon.jasm;

public final class Constant<T> {

    public final T value;

    public Constant(T value) {
        this.value = value;
    }

    public String toString() {
        if (value instanceof JType) {
            return value.toString();
        } else if (value instanceof Method) {
            return ((Method) value).descriptor.niceString(((Method) value).name);
        } else if (value instanceof Field) {
            return ((Field) value).name + " " + ((Field) value).type.niceName;
        } else if (value instanceof Local) {
            return "v" + ((Local) value).index;
        }
        return value.toString();
    }

}
