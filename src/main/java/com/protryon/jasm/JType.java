package com.protryon.jasm;

import com.shapesecurity.functional.Pair;

public class JType {

    public final String niceName;
    public final String javaName;

    private JType(String niceName, String javaName) {
        this.niceName = niceName;
        this.javaName = javaName;
    }

    public static final JType voidT = new JType("void", "V");
    public static final JType byteT = new JType("int", "B");
    public static final JType charT = new JType("char", "C");
    public static final JType shortT = new JType("short", "S");
    public static final JType intT = new JType("int", "I");
    public static final JType longT = new JType("long", "J");
    public static final JType floatT = new JType("float", "F");
    public static final JType doubleT = new JType("double", "D");
    public static final JType booleanT = new JType("boolean", "Z");

    public static final class JTypeInstance extends JType {
        public final Klass klass;

        private JTypeInstance(Klass klass) {
            super(klass.name, "L" + klass.name + ";");
            this.klass = klass;
        }
    }

    public static final class JTypeArray extends JType {
        public final JType elementType;

        private JTypeArray(JType elementType) {
            super(elementType.niceName + "[]", "[" + elementType.javaName);
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

    public int hashCode() {
        return this.javaName.hashCode() + 1;
    }

}
