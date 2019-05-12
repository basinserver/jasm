package com.protryon.jasm;

import com.shapesecurity.functional.Pair;

import java.util.Arrays;
import java.util.Stack;

public class JType {

    public final String niceName;
    public final String javaName;
    public final int computationType;

    private JType(String niceName, String javaName, int computationType) {
        this.niceName = niceName;
        this.javaName = javaName;
        this.computationType = computationType;
    }

    public static final JType voidT = new JType("void", "V", -1);
    public static final JType byteT = new JType("int", "B", 1);
    public static final JType charT = new JType("char", "C", 1);
    public static final JType shortT = new JType("short", "S", 1);
    public static final JType intT = new JType("int", "I", 1);
    public static final JType longT = new JType("long", "J", 2);
    public static final JType floatT = new JType("float", "F", 1);
    public static final JType doubleT = new JType("double", "D", 2);
    public static final JType booleanT = new JType("boolean", "Z", 1);
    public static final JType nullT = new JType("null", "N", -1); // internal, special case for general, untyped null

    public static final class JTypeInstance extends JType {
        public final Klass klass;

        private JTypeInstance(Klass klass) {
            super(klass.name, "L" + klass.name + ";", 1);
            this.klass = klass;
        }
    }

    public static final class JTypeArray extends JType {
        public final JType elementType;

        private JTypeArray(JType elementType) {
            super(elementType.niceName + "[]", "[" + elementType.javaName, 1);
            this.elementType = elementType;
        }

    }

    public static JType instance(Klass klass) {
        return new JTypeInstance(klass);
    }

    public static JType array(JType type) {
        return new JTypeArray(type);
    }

    public static JType fromDescriptor(Classpath classpath, String descriptor) {
        return fromDescriptorWithLength(classpath, descriptor).left;
    }

    public static Pair<JType, Integer> fromDescriptorWithLength(Classpath classpath, String descriptor) {
        switch (descriptor.charAt(0)) {
            case 'V':
                return Pair.of(voidT, 1);
            case 'B':
                return Pair.of(byteT, 1);
            case 'C':
                return Pair.of(charT, 1);
            case 'S':
                return Pair.of(shortT, 1);
            case 'I':
                return Pair.of(intT, 1);
            case 'J':
                return Pair.of(longT, 1);
            case 'F':
                return Pair.of(floatT, 1);
            case 'D':
                return Pair.of(doubleT, 1);
            case 'Z':
                return Pair.of(booleanT, 1);
            case '[':
                Pair<JType, Integer> pair = fromDescriptorWithLength(classpath, descriptor.substring(1));
                return pair.mapLeft(JType::array).mapRight(x -> x + 1);
            case 'L':
                int semiIndex = descriptor.indexOf(";", 1);
                String className = descriptor.substring(1, semiIndex);
                return Pair.of(new JTypeInstance(classpath.loadKlass(className)), className.length() + 2);
            default:
                throw new RuntimeException("Invalid descriptor: " + descriptor.charAt(0));
        }
    }

    public final String toString() {
        return this.niceName;
    }

    public String toDescriptor() {
        return this.javaName;
    }

    public boolean equals(Object o) {
        return o instanceof JType && ((JType) o).javaName.equals(this.javaName);
    }

    public boolean assignableTo(Object o) {
        if (this == JType.nullT && (o instanceof JTypeInstance || o instanceof JTypeArray)) {
            return true;
        }
        if (!(o instanceof JTypeInstance) && !(this instanceof JTypeInstance) && !(o instanceof JTypeArray) && !(this instanceof JTypeArray)) {
            return true;
        }
        if (!(o instanceof JTypeInstance) || !(this instanceof JTypeInstance)) {
            return this.equals(o);
        }
        JTypeInstance other = (JTypeInstance) o;
        if (other.javaName.equals(this.javaName)) {
            return true;
        }
        Klass assigningKlass = ((JTypeInstance) this).klass;
        Klass currentKlass = other.klass;
        Stack<Klass> awaitingKlass = new Stack<>();
        awaitingKlass.push(assigningKlass);
        while (!awaitingKlass.isEmpty()) {
            Klass klass = awaitingKlass.pop();
            if (currentKlass == klass) {
                return true;
            }
            if (klass.extending != null) {
                awaitingKlass.push(klass.extending);
            }
            klass.interfaces.forEach(awaitingKlass::push);
        }
        return false;
    }

    public int hashCode() {
        return this.javaName.hashCode() + 1;
    }

    public JType elementType() {
        if (!(this instanceof JTypeArray)) {
            throw new RuntimeException("Illegal non-array when expecting array type: " + this.toString());
        }
        return ((JTypeArray) this).elementType;
    }

    public Klass referenceOf() {
        if (this == nullT) {
            return null;
        }
        if (!(this instanceof JTypeInstance)) {
            throw new RuntimeException("Illegal non-instance when expecting instance type: " + this.toString());
        }
        return ((JTypeInstance) this).klass;
    }

    public void assertReference() {
        if (this != nullT && !(this instanceof JTypeInstance) && !(this instanceof JTypeArray)) {
            throw new RuntimeException("Illegal non-instance when expecting instance type: " + this.toString());
        }
    }

    public void assertType(JType other) {
        if (!this.equals(other)) {
            throw new RuntimeException("Got type \"" + this.niceName + "\" when expecting type \"" + (other == null ? "NULL" : other.niceName) + "\"");
        }
    }

    public void assertAssignableTo(JType to) {
        if (!this.assignableTo(to)) {
            throw new RuntimeException("Got type \"" + this.niceName + "\" when expecting type \"" + (to == null ? "NULL" : to.niceName) + "\"");
        }
    }

    public boolean isTypes(JType... others) {
        return Arrays.asList(others).contains(this);
    }

    public void assertTypes(JType... others) {
        if (!isTypes(others)) {
            throw new RuntimeException("Got type \"" + this.niceName + "\" when expecting type other types.");
        }
    }

    public void assertComputationType(int type) {
        if (this.computationType != type) {
            throw new RuntimeException("Got type \"" + this.niceName + "\" when expecting computation type \"" + type + "\"");
        }
    }
}
