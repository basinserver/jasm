package com.protryon.jasm.stage1;

import com.protryon.jasm.*;
import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.OpcodeTable;
import com.protryon.jasm.instruction.psuedoinstructions.EnterTry;
import com.protryon.jasm.instruction.psuedoinstructions.ExitTry;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.protryon.jasm.stage1.constant.CClass;
import com.protryon.jasm.stage1.constant.CUTF8;
import com.protryon.jasm.util.TrackingByteArrayInputStream;

import java.io.*;
import java.util.*;

public class Stage1Class {

    private int minorVersion, majorVersion;
    private ArrayList<CConstant> constants = new ArrayList<>();
    private ArrayList<AttributeHolder> fields = new ArrayList<>();
    private ArrayList<AttributeHolder> methods = new ArrayList<>();
    private ArrayList<Attribute> attributes = new ArrayList<>();
    private ArrayList<Integer> implementing = new ArrayList<>();
    private int name;
    private int extending;
    private int accessFlags;

    public Stage1Class(int minorVersion, int majorVersion, ArrayList<CConstant> constants, ArrayList<AttributeHolder> fields, ArrayList<AttributeHolder> methods, ArrayList<Attribute> attributes, ArrayList<Integer> implementing, int name, int extending, int accessFlags) {
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constants = constants;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
        this.implementing = implementing;
        this.name = name;
        this.extending = extending;
        this.accessFlags = accessFlags;
    }

    public Stage1Class(DataInputStream in, boolean close) throws IOException {
        if (in.read() != 0x000000CA || in.read() != 0x000000FE || in.read() != 0x000000BA || in.read() != 0x000000BE) {
            throw new IOException("Not a Class File! Magic is not 0xCAFEBABE.");
        }
        minorVersion = in.readUnsignedShort();
        majorVersion = in.readUnsignedShort();
        int constantCount = in.readUnsignedShort();
        this.constants.ensureCapacity(constantCount);
        this.constants.add(null);
        for (int i = 1; i < constantCount; ++i) {
            CConstant c = CConstant.read(in);
            this.constants.add(c);
            if (c.isDoubled()) {
                this.constants.add(c);
                ++i;
            }
        }

        accessFlags = in.readUnsignedShort();
        this.name = in.readUnsignedShort();
        this.extending = in.readUnsignedShort();

        int interface_count = in.readUnsignedShort();
        this.implementing.ensureCapacity(interface_count);
        for (int i = 0; i < interface_count; i++) {
            this.implementing.add(in.readUnsignedShort());
        }
        int fieldCount = in.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            this.fields.add(AttributeHolder.read(in));
        }
        int methodCount = in.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            this.methods.add(AttributeHolder.read(in));
        }
        int attributeCount = in.readUnsignedShort();
        for (int i = 0; i < attributeCount; i++) {
            this.attributes.add(new Attribute(in));
        }
        if (close) {
            in.close();
        }
    }

    public void write(DataOutputStream out) throws IOException {
        out.write(0x000000CA);
        out.write(0x000000FE);
        out.write(0x000000BA);
        out.write(0x000000BE);
        out.writeShort(minorVersion);
        out.writeShort(majorVersion);
        out.writeShort(constants.size());
        boolean lastDoubled = false;
        for (CConstant CConstant : constants) {
            if (CConstant.isDoubled()) {
                if (!lastDoubled) {
                    lastDoubled = true;
                } else {
                    lastDoubled = false;
                    continue;
                }
                out.write(CConstant.type);
            }
            CConstant.write(out);
        }
        out.writeShort(accessFlags);
        out.writeShort(name);
        out.writeShort(extending);
        out.writeShort(implementing.size());
        for (Integer i : implementing) {
            out.writeShort(i);
        }
        out.writeShort(fields.size());
        for (AttributeHolder field : this.fields) {
            field.write(out);
        }
        out.writeShort(methods.size());
        for (AttributeHolder method : methods) {
            method.write(out);
        }
        out.writeShort(attributes.size());
        for (Attribute attribute : attributes) {
            attribute.write(out);
        }
    }

    private Klass resolveKlass(Classpath classpath, int classRef) {
        return classpath.loadKlass(CUTF8.assertCUTF8(this.constants.get(CClass.assertCClass(this.constants.get(classRef)).value)).value);
    }

    private Constant resolveConstant(Classpath classpath, ArrayList<Constant> constants, int x) {
        if (constants.size() <= x || constants.get(x) == null) {
            Constant c = this.constants.get(x).toConstant(classpath, y -> resolveConstant(classpath, constants, (Integer) y));
            constants.set(x, c);
            return c;
        } else {
            return constants.get(x);
        }
    }

    public void finishClass(Classpath classpath, Klass klass) throws IOException {
        ArrayList<Constant> constants = new ArrayList<>(this.constants.size());
        for (int i = 0; i < this.constants.size(); ++i) {
            constants.add(null);
        }
        for (int i = 1; i < this.constants.size(); ++i) {
            if (constants.size() > i && constants.get(i) != null) {
                continue;
            }
            resolveConstant(classpath, constants, i);
        }
        for (AttributeHolder ourField : this.fields) {
            Field newField = klass.fields.get(CUTF8.assertCUTF8(this.constants.get(ourField.name)).value);

            if (newField.isStatic) {
                for (Attribute attribute : ourField.attributes) {
                    if (CUTF8.assertCUTF8(this.constants.get(attribute.attribute_name_index)).value.equals("ConstantValue")) {
                        byte[] data = attribute.attribute_info;
                        if (data.length != 2) {
                            break;
                        }
                        int value = (Byte.toUnsignedInt(data[0]) << 8) | Byte.toUnsignedInt(data[1]);
                        newField.constantValue = constants.get(value);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < this.methods.size(); ++i) {
            AttributeHolder ourMethod = this.methods.get(i);
            Method newMethod = klass.methods.get(i);

            for (Attribute attribute : ourMethod.attributes) {
                if (CUTF8.assertCUTF8(this.constants.get(attribute.attribute_name_index)).value.equals("Code")) {
                    byte[] data = attribute.attribute_info;
                    if (data.length < 10) {
                        break;
                    }
                    TrackingByteArrayInputStream trackingIn = new TrackingByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(trackingIn);
                    int maxStack = in.readUnsignedShort();
                    int maxLocals = in.readUnsignedShort();
                    int codeLength = in.readInt();
                    boolean wide = false;
                    HashMap<Integer, Label> labelsToAdd = new HashMap<>();
                    HashMap<Integer, Integer> instructionPCToIndex = new HashMap<>();
                    for (int pc = 0; pc < codeLength; pc = trackingIn.getPosition() - 8) {
                        int op = in.read();
                        if (op < 0 || op >= OpcodeTable.suppliers.length) {
                            throw new UnsupportedOperationException("Illegal opcode: " + op);
                        }
                        if (op == 196 && !wide) { // Wide instruction
                            wide = true;
                            continue;
                        }
                        Instruction ins = OpcodeTable.suppliers[op].get();
                        ins.read(wide, constants, newMethod, x -> {
                            Label label = newMethod.getOrMakeLabel("l_" + x);
                            labelsToAdd.put(x, label);
                            return label;
                        }, pc, in);
                        wide = false;
                        instructionPCToIndex.put(pc, newMethod.code.size());
                        newMethod.code.add(ins);
                    }
                    HashMap<Integer, Instruction> instructionsToAdd = new HashMap<>();
                    labelsToAdd.forEach((pc, label) -> {
                        instructionsToAdd.put(instructionPCToIndex.get(pc), label);
                    });
                    //TODO: practically wont happen but possible collision ^^
                    int exceptionTableCount = in.readUnsignedShort();
                    for (int j = 0; j < exceptionTableCount; ++j) {
                        int start = in.readUnsignedShort();
                        int end = in.readUnsignedShort();
                        int handler = in.readUnsignedShort();
                        int type = in.readUnsignedShort();
                        int startIndex = instructionPCToIndex.get(start);
                        int endIndex = end == codeLength ? -1 : instructionPCToIndex.get(end);
                        int handlerIndex = instructionPCToIndex.get(handler);
                        JType jtype;
                        if (type == 0) {
                            jtype = JType.voidT;
                        } else {
                            jtype = JType.instance((Klass) constants.get(type).value);
                        }
                        Label label = newMethod.getOrMakeLabel("_catch_" + startIndex + "_" + endIndex + "_" + jtype.javaName);
                        EnterTry enter = new EnterTry(label, jtype);
                        ExitTry exit = new ExitTry();
                        instructionsToAdd.put(startIndex, enter);
                        instructionsToAdd.put(handlerIndex, label);
                        if (endIndex == -1) {
                            newMethod.code.add(exit);
                        }
                    }
                    ListIterator<Instruction> iterator = newMethod.code.listIterator();
                    int unmodifiedIndex = 0;
                    while (iterator.hasNext()) {
                        Instruction maybeAdd = instructionsToAdd.get(unmodifiedIndex++);
                        if (maybeAdd != null) {
                            iterator.add(maybeAdd);
                        }
                        iterator.next();
                    }
                    break;
                }
            }
        }

    }

    public void midClass(Classpath classpath, Klass klass) {
        if (this.extending > 0) {
            klass.extending = resolveKlass(classpath, this.extending);
        }
        klass.interfaces.ensureCapacity(this.implementing.size());
        for (Integer interfaceRef : this.implementing) {
            klass.interfaces.add(resolveKlass(classpath, interfaceRef));
        }
        for (AttributeHolder attributeHolder : this.fields) {
            Field field = new Field(klass, JType.fromDescriptor(classpath, CUTF8.assertCUTF8(this.constants.get(attributeHolder.descriptor)).value), CUTF8.assertCUTF8(this.constants.get(attributeHolder.name)).value);
            if ((attributeHolder.accessFlags & 0x0001) != 0) {
                field.isPublic = true;
            }
            if ((attributeHolder.accessFlags & 0x0002) != 0) {
                field.isPrivate = true;
            }
            if ((attributeHolder.accessFlags & 0x0004) != 0) {
                field.isProtected = true;
            }
            if ((attributeHolder.accessFlags & 0x0008) != 0) {
                field.isStatic = true;
            }
            if ((attributeHolder.accessFlags & 0x0010) != 0) {
                field.isFinal = true;
            }
            if ((attributeHolder.accessFlags & 0x0040) != 0) {
                field.isVolatile = true;
            }
            if ((attributeHolder.accessFlags & 0x0080) != 0) {
                field.isTransient = true;
            }
            if ((attributeHolder.accessFlags & 0x1000) != 0) {
                field.isSynthetic = true;
            }
            if ((attributeHolder.accessFlags & 0x4000) != 0) {
                field.isEnum = true;
            }
            klass.fields.put(field.name, field);
        }
        for (AttributeHolder attributeHolder : this.methods) {
            Method method = new Method(klass, CUTF8.assertCUTF8(this.constants.get(attributeHolder.name)).value, MethodDescriptor.fromString(classpath, CUTF8.assertCUTF8(this.constants.get(attributeHolder.descriptor)).value));
            if ((attributeHolder.accessFlags & 0x0001) != 0) {
                method.isPublic = true;
            }
            if ((attributeHolder.accessFlags & 0x0002) != 0) {
                method.isPrivate = true;
            }
            if ((attributeHolder.accessFlags & 0x0004) != 0) {
                method.isProtected = true;
            }
            if ((attributeHolder.accessFlags & 0x0008) != 0) {
                method.isStatic = true;
            }
            if ((attributeHolder.accessFlags & 0x0010) != 0) {
                method.isFinal = true;
            }
            if ((attributeHolder.accessFlags & 0x0020) != 0) {
                method.isSynchronized = true;
            }
            if ((attributeHolder.accessFlags & 0x0040) != 0) {
                method.isBridge = true;
            }
            if ((attributeHolder.accessFlags & 0x0080) != 0) {
                method.isVarargs = true;
            }
            if ((attributeHolder.accessFlags & 0x0100) != 0) {
                method.isNative = true;
            }
            if ((attributeHolder.accessFlags & 0x0400) != 0) {
                method.isAbstract = true;
            }
            if ((attributeHolder.accessFlags & 0x0800) != 0) {
                method.isStrict = true;
            }
            if ((attributeHolder.accessFlags & 0x1000) != 0) {
                method.isSynthetic = true;
            }
            klass.methods.add(method);
        }
    }

    public Klass preClass() {
        return new Klass(majorVersion, minorVersion, CUTF8.assertCUTF8(this.constants.get(CClass.assertCClass(this.constants.get(this.name)).value)).value,
            (accessFlags & 0x1) != 0, (accessFlags & 0x10) != 0, (accessFlags & 0x20) != 0, (accessFlags & 0x200) != 0, (accessFlags & 0x400) != 0, (accessFlags & 0x1000) != 0, (accessFlags & 0x2000) != 0, (accessFlags & 0x4000) != 0, (accessFlags & 0x8000) != 0);
    }

}
