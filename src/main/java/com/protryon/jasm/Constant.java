package com.protryon.jasm;

public final class Constant<T> {

    public final T value;

    public Constant(T value) {
        this.value = value;
    }

    public String toString() {
        if (value instanceof Klass) {
            return ((Klass) value).name;
        } else if (value instanceof Method) {
            return ((Method) value).descriptor.niceString(((Method) value).name);
        } else if (value instanceof Field) {
            return ((Field) value).name + " " + ((Field) value).type.niceName;
        }
        return value.toString();
    }

}
