package com.protryon.jasm;

import com.protryon.jasm.stage1.Stage1Class;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class Klass {

    public int majorVersion;
    public int minorVersion;
    public String name;
    public Klass extending = null;
    public ArrayList<Klass> interfaces = new ArrayList<>();
    public LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
    public ArrayList<Method> methods = new ArrayList<>();

    public boolean isPublic = false;
    public boolean isFinal = false;
    public boolean isSuper = false;
    public boolean isInterface = false;
    public boolean isAbstract = false;
    public boolean isSynthetic = false;
    public boolean isAnnotation = false;
    public boolean isEnum = false;
    public boolean isModule = false;


    public Klass(int majorVersion, int minorVersion, String name) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.name = name;
    }

    public Klass(int majorVersion, int minorVersion, String name, boolean isPublic, boolean isFinal, boolean isSuper, boolean isInterface, boolean isAbstract, boolean isSynthetic, boolean isAnnotation, boolean isEnum, boolean isModule) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.name = name;
        this.isPublic = isPublic;
        this.isFinal = isFinal;
        this.isSuper = isSuper;
        this.isInterface = isInterface;
        this.isAbstract = isAbstract;
        this.isSynthetic = isSynthetic;
        this.isAnnotation = isAnnotation;
        this.isEnum = isEnum;
        this.isModule = isModule;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class name: ").append(name).append("\n");
        if (extending != null) {
            sb.append("extending: ").append(extending.name).append("\n");
        }
        if (interfaces.size() > 0) {
            sb.append("implementing: ");
            for (Klass klass : interfaces) {
                sb.append(klass.name).append(", ");
            }
            sb.append("\n");
        }
        fields.forEach((name, field) -> {
            if (field.isStatic) {
                sb.append("static ");
            }
            sb.append(field.type.niceName);
            sb.append(" ").append(field.name);
            sb.append(";\n\n");
        });
        sb.append("\n\n\n");
        for (Method method : methods) {
            if (method.isStatic) {
                sb.append("static ");
            }
            sb.append(method.descriptor.niceString(method.name)).append(";\n\n");
        }
        return sb.toString();
    }
}
