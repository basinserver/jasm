package com.protryon.jasm;

public class MethodHandle {

    public final Constant ref;
    public final MethodHandleType refType;

    public MethodHandle(Constant ref, MethodHandleType refType) {
        this.ref = ref;
        this.refType = refType;
    }

    public enum MethodHandleType {
        NULL,
        REF_getField,
        REF_getStatic,
        REF_putField,
        REF_putStatic,
        REF_invokeVirtual,
        REF_invokeStatic,
        REF_invokeSpecial,
        REF_newInvokeSpecial,
        REF_invokeInterface,
    }

}
