package com.protryon.jasm;

public final class AType {

    public final JType type;
    public final int index;

    private AType(JType type, int index) {
        this.type = type;
        this.index = index;
    }

    public static final AType BOOLEAN = new AType(JType.booleanT, 4);
    public static final AType CHAR = new AType(JType.charT, 5);
    public static final AType FLOAT = new AType(JType.floatT, 6);
    public static final AType DOUBLE = new AType(JType.doubleT, 7);
    public static final AType BYTE = new AType(JType.byteT, 8);
    public static final AType SHORT = new AType(JType.shortT, 9);
    public static final AType INT = new AType(JType.intT, 10);
    public static final AType LONG = new AType(JType.longT, 11);

    public static AType from(int x) {
        switch (x) {
            case 4:
                return BOOLEAN;
            case 5:
                return CHAR;
            case 6:
                return FLOAT;
            case 7:
                return DOUBLE;
            case 8:
                return BYTE;
            case 9:
                return SHORT;
            case 10:
                return INT;
            case 11:
                return LONG;
            default:
                throw new UnsupportedOperationException("invalid atype: " + x);
        }
    }

}
