package com.protryon.jasm;

import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.instruction.psuedoinstructions.CatchLabel;
import com.protryon.jasm.instruction.psuedoinstructions.Label;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Method {

    public final Klass parent;
    public String name;
    public MethodDescriptor descriptor;
    public boolean isPublic = false;
    public boolean isPrivate = false;
    public boolean isProtected = false;
    public boolean isStatic = false;
    public boolean isFinal = false;
    public boolean isSynchronized = false;
    public boolean isBridge = false;
    public boolean isVarargs = false;
    public boolean isNative = false;
    public boolean isAbstract = false;
    public boolean isStrict = false;
    public boolean isSynthetic = false;

    // set for created methods in things like stdlib or unincluded libs
    public boolean isDummy = false;

    public int tempVariableCounter = 0;

    public LinkedList<Instruction> code = new LinkedList<>();
    public LinkedHashMap<String, Label> labels = new LinkedHashMap<>();

    public Label getOrMakeLabel(String name) {
        if (labels.containsKey(name)) {
            return labels.get(name);
        }
        Label label = new Label(name);
        labels.put(name, label);
        return label;
    }

    public CatchLabel makeCatch(String name) {
        if (labels.containsKey(name)) {
            return (CatchLabel) labels.get(name);
        }
        CatchLabel label = new CatchLabel(name);
        labels.put(name, label);
        return label;
    }


    public Method(Klass parent, String name, MethodDescriptor descriptor) {
        this.parent = parent;
        this.name = name;
        this.descriptor = descriptor;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isPublic) {
            sb.append("public ");
        }
        if (this.isPrivate) {
            sb.append("private ");
        }
        if (this.isProtected) {
            sb.append("protected ");
        }
        if (this.isStatic) {
            sb.append("static ");
        }
        if (this.isFinal) {
            sb.append("final ");
        }
        if (this.isSynchronized) {
            sb.append("synchronized ");
        }
        if (this.isNative) {
            sb.append("native ");
        }
        if (this.isAbstract) {
            sb.append("abstract ");
        }
        sb.append(this.descriptor.niceString(this.name)).append(" {\n");
        for (Instruction instruction : this.code) {
            sb.append("  ").append(instruction.toString()).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

}
