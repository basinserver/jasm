package com.protryon.jasm

class MethodHandle(val ref: Constant<*>, val refType: MethodHandleType) {

    enum class MethodHandleType {
        NULL,
        REF_getField,
        REF_getStatic,
        REF_putField,
        REF_putStatic,
        REF_invokeVirtual,
        REF_invokeStatic,
        REF_invokeSpecial,
        REF_newInvokeSpecial,
        REF_invokeInterface
    }

}
