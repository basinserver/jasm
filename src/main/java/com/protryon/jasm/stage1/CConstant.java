package com.protryon.jasm.stage1;

import com.protryon.jasm.*;
import com.protryon.jasm.Method;
import com.protryon.jasm.stage1.constant.*;
import com.shapesecurity.functional.F;
import com.shapesecurity.functional.data.Either;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class CConstant<T> {

    public final T value;
    public final int type;

    protected CConstant(T value, int type) {
        this.value = value;
        this.type = type;
    }

    protected abstract void write(DataOutputStream out) throws IOException;

    protected static CConstant read(DataInputStream in) throws IOException {
        int type = in.read();
        switch (type) {
            case 1:
                byte[] utf = new byte[in.readUnsignedShort()];
                in.readFully(utf);
                return new CUTF8(new String(utf));
            case 3:
                return new CInteger(in.readInt());
            case 4:
                return new CFloat(in.readFloat());
            case 5:
                return new CLong(in.readLong());
            case 6:
                return new CDouble(in.readDouble());
            case 7:
                return new CClass(in.readUnsignedShort());
            case 8:
                return new CString(in.readUnsignedShort());
            case 9:
                return new CFieldRef(in.readUnsignedShort(), in.readUnsignedShort());
            case 10:
                return new CMethodRef(in.readUnsignedShort(), in.readUnsignedShort());
            case 11:
                return new CInterfaceMethodRef(in.readUnsignedShort(), in.readUnsignedShort());
            case 12:
                return new CNameAndType(in.readUnsignedShort(), in.readUnsignedShort());
            case 15:
                return new CMethodHandle((byte) in.read(), in.readUnsignedShort());
            case 16:
                return new CMethodType(in.readUnsignedShort());
            case 17:
                return new CDynamic(in.readUnsignedShort(), in.readUnsignedShort());
            case 18:
                return new CInvokeDynamic(in.readUnsignedShort(), in.readUnsignedShort());
            case 19:
                return new CModule(in.readUnsignedShort());
            case 20:
                return new CPackage(in.readUnsignedShort());
            default:
                throw new RuntimeException("Invalid constant type: " + type);
        }
    }

    private Method findMethod(Klass klass, String name, MethodDescriptor descriptor) {
        for (Method method : klass.methods) {
            if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                return method;
            }
        }
        if (klass.extending != null) {
            Method method = findMethod(klass.extending, name, descriptor);
            if (method != null) {
                return method;
            }
        }
        for (Klass implementing : klass.interfaces) {
            Method method = findMethod(implementing, name, descriptor);
            if (method != null) {
                return method;
            }
        }
        Method method = new Method(name, descriptor);
        method.isDummy = true;
        klass.methods.add(method);
        return method;
    }

    private Field findField(Klass klass, String name, JType type) {
        Field field = klass.fields.get(name);
        if (field != null) {
            return field;
        }
        if (klass.extending != null) {
            field = findField(klass.extending, name, type);
            if (field != null) {
                return field;
            }
        }
        for (Klass implementing : klass.interfaces) {
            field = findField(implementing, name, type);
            if (field != null) {
                return field;
            }
        }
        field = new Field(type, name);
        field.isDummy = true;
        klass.fields.put(name, field);
        return field;
    }

    protected Constant toConstant(Classpath classpath, F<Integer, Constant> lookup) {
        NameAndType nameAndType;
        switch (this.type) {
            case 1:
                return new Constant<>(((CUTF8) this).value);
            case 3:
                return new Constant<>(((CInteger) this).value);
            case 4:
                return new Constant<>(((CFloat) this).value);
            case 5:
                return new Constant<>(((CLong) this).value);
            case 6:
                return new Constant<>(((CDouble) this).value);
            case 7:
                return new Constant<>(classpath.loadKlass((String) lookup.apply(((CClass) this).value).value));
            case 8:
                return new Constant<>((String) lookup.apply(((CString) this).value).value);
            case 9:
                CFieldRef fieldRef = (CFieldRef) this;
                Klass fieldKlass = (Klass) lookup.apply(fieldRef.value.left).value;
                nameAndType = (NameAndType) lookup.apply(fieldRef.value.right).value;
                Field field = findField(fieldKlass, nameAndType.name, nameAndType.type.left().fromJust());
                if (field == null) {
                    throw new RuntimeException("Field not found: " + nameAndType.toString());
                }
                /*if (!field.type.equals(nameAndType.type.left().fromJust())) {
                    throw new RuntimeException("Type mismatch in field reference");
                }*/
                return new Constant<>(field);
            case 10: {
                CMethodRef methodRef = (CMethodRef) this;
                Klass methodKlass = (Klass) lookup.apply(methodRef.value.left).value;
                nameAndType = (NameAndType) lookup.apply(methodRef.value.right).value;
                Method method = findMethod(methodKlass, nameAndType.name, nameAndType.type.right().fromJust());
                if (method == null) {
                    throw new RuntimeException("Method descriptor not found: " + nameAndType.toString());
                }
                return new Constant<>(method);
            }
            case 11: {
                CInterfaceMethodRef methodRef = (CInterfaceMethodRef) this;
                Klass methodKlass = (Klass) lookup.apply(methodRef.value.left).value;
                nameAndType = (NameAndType) lookup.apply(methodRef.value.right).value;
                Method method = findMethod(methodKlass, nameAndType.name, nameAndType.type.right().fromJust());
                if (method == null) {
                    throw new RuntimeException("Interface method descriptor not found: " + nameAndType.toString());
                }
                return new Constant<>(method);
            }
            case 12:
                CNameAndType cNameAndType = (CNameAndType) this;
                String name = (String) lookup.apply(cNameAndType.value.left).value;
                String rawDescriptor = (String) lookup.apply(cNameAndType.value.right).value;
                if (rawDescriptor.startsWith("(")) {
                    return new Constant<>(new NameAndType(name, Either.right(MethodDescriptor.fromString(classpath, rawDescriptor))));
                }
                return new Constant<>(new NameAndType(name, Either.left(JType.fromDescriptor(classpath, rawDescriptor))));
            case 15:
                CMethodHandle handle = (CMethodHandle) this;
                return new Constant<>(new MethodHandle(lookup.apply(handle.value.right), MethodHandle.MethodHandleType.values()[handle.value.left]));
            case 16:
                CMethodType methodType = (CMethodType) this;
                return new Constant<>(MethodDescriptor.fromString(classpath, (String) lookup.apply(methodType.value).value));
            case 17:
                throw new UnsupportedOperationException("CDynamic");
                //return new CDynamic(in.readUnsignedShort(), in.readUnsignedShort());
            case 18:
                return new Constant<>((CInvokeDynamic) this); // TODO: implement
                // throw new UnsupportedOperationException("CInvokeDynamic");
            case 19:
                throw new UnsupportedOperationException("CModule");
                //return new CModule(in.readUnsignedShort());
            case 20:
                throw new UnsupportedOperationException("CPackage");
                //return new CPackage(in.readUnsignedShort());
            default:
                throw new UnsupportedOperationException("Invalid constant type: " + type);
        }
    }

    public boolean isDoubled() {
        return false;
    }
}
