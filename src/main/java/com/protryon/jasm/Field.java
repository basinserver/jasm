package com.protryon.jasm;

public class Field {

    public JType type;
    public String name;
    public boolean isPublic = false;
    public boolean isPrivate = false;
    public boolean isProtected = false;
    public boolean isStatic = false;
    public boolean isFinal = false;
    public boolean isVolatile = false;
    public boolean isTransient = false;
    public boolean isSynthetic = false;
    public boolean isEnum = false;
    public Constant constantValue;

    // set for created methods in things like stdlib or unincluded libs
    public boolean isDummy = false;

    public Field(JType type, String name) {
        this.type = type;
        this.name = name;
    }

}
