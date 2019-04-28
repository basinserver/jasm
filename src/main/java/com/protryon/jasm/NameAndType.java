package com.protryon.jasm;

import com.shapesecurity.functional.data.Either;

public class NameAndType {

    public final String name;
    public final Either<JType, MethodDescriptor> type;

    public NameAndType(String name, Either<JType, MethodDescriptor> type) {
        this.name = name;
        this.type = type;
    }

    public String toString() {
        return this.name + " " + type.either(JType::toString, MethodDescriptor::toString);
    }

}
