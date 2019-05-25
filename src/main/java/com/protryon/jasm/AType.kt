package com.protryon.jasm

class AType private constructor(val type: JType, val index: Int) {
    companion object {

        val BOOLEAN = AType(JType.booleanT, 4)
        val CHAR = AType(JType.charT, 5)
        val FLOAT = AType(JType.floatT, 6)
        val DOUBLE = AType(JType.doubleT, 7)
        val BYTE = AType(JType.byteT, 8)
        val SHORT = AType(JType.shortT, 9)
        val INT = AType(JType.intT, 10)
        val LONG = AType(JType.longT, 11)

        fun from(x: Int): AType {
            when (x) {
                4 -> return BOOLEAN
                5 -> return CHAR
                6 -> return FLOAT
                7 -> return DOUBLE
                8 -> return BYTE
                9 -> return SHORT
                10 -> return INT
                11 -> return LONG
                else -> throw UnsupportedOperationException("invalid atype: $x")
            }
        }
    }

}
