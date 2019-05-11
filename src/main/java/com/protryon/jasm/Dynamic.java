package com.protryon.jasm;

public class Dynamic {

    public final boolean isMethod;
    public final BootstrapMethod bootstrapMethod;
    public final NameAndType nameAndType;


    public Dynamic(boolean isMethod, BootstrapMethod bootstrapMethod, NameAndType nameAndType) {
        this.isMethod = isMethod;
        this.bootstrapMethod = bootstrapMethod;
        this.nameAndType = nameAndType;
    }
}
