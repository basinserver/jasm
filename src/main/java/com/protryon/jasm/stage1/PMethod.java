package com.protryon.jasm.stage1;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class PMethod {

    private int accessFlags = 0;
    public int name_index, descriptor_index ;
    public ArrayList<Attribute> attributes = new ArrayList<>();

    public PMethod(int name_index, int descriptor_index) {
        this.name_index = name_index;
        this.descriptor_index = descriptor_index;
    }

    public boolean isPublic() {
        return (accessFlags & 0x0001) == 0x0001;
    }

    public void setPublic(boolean n) {
        boolean c = isPublic();
        if (c && !n) {
            accessFlags = accessFlags - 0x0001;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0001;
        }
    }

    public boolean isPrivate() {
        return (accessFlags & 0x0002) == 0x0002;
    }

    public void setPrivate(boolean n) {
        boolean c = isPrivate();
        if (c && !n) {
            accessFlags = accessFlags - 0x0002;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0002;
        }
    }

    public boolean isProtected() {
        return (accessFlags & 0x0004) == 0x0004;
    }

    public void setProtected(boolean n) {
        boolean c = isProtected();
        if (c && !n) {
            accessFlags = accessFlags - 0x0004;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0004;
        }
    }

    public boolean isStatic() {
        return (accessFlags & 0x0008) == 0x0008;
    }

    public void setStatic(boolean n) {
        boolean c = isStatic();
        if (c && !n) {
            accessFlags = accessFlags - 0x0008;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0008;
        }
    }

    public boolean isFinal() {
        return (accessFlags & 0x0010) == 0x0010;
    }

    public void setFinal(boolean n) {
        boolean c = isFinal();
        if (c && !n) {
            accessFlags = accessFlags - 0x0010;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0010;
        }
    }

    public boolean isSynchronized() {
        return (accessFlags & 0x0020) == 0x0020;
    }

    public void setSynchronized(boolean n) {
        boolean c = isSynchronized();
        if (c && !n) {
            accessFlags = accessFlags - 0x0020;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0020;
        }
    }

    public boolean isBridge() {
        return (accessFlags & 0x0040) == 0x0040;
    }

    public void setBridge(boolean n) {
        boolean c = isBridge();
        if (c && !n) {
            accessFlags = accessFlags - 0x0040;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0040;
        }
    }

    public boolean isVarArgs() {
        return (accessFlags & 0x0080) == 0x0080;
    }

    public void setVarArgs(boolean n) {
        boolean c = isVarArgs();
        if (c && !n) {
            accessFlags = accessFlags - 0x0080;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0080;
        }
    }

    public boolean isNative() {
        return (accessFlags & 0x0100) == 0x0100;
    }

    public void setNative(boolean n) {
        boolean c = isNative();
        if (c && !n) {
            accessFlags = accessFlags - 0x0100;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0100;
        }
    }

    public boolean isAbstract() {
        return (accessFlags & 0x0400) == 0x0400;
    }

    public void setAbstract(boolean n) {
        boolean c = isAbstract();
        if (c && !n) {
            accessFlags = accessFlags - 0x0400;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0400;
        }
    }

    public boolean isStrict() {
        return (accessFlags & 0x0800) == 0x0800;
    }

    public void setStrict(boolean n) {
        boolean c = isAbstract();
        if (c && !n) {
            accessFlags = accessFlags - 0x0800;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x0800;
        }
    }

    public boolean isSynthetic() {
        return (accessFlags & 0x1000) == 0x1000;
    }

    public void setSynthetic(boolean n) {
        boolean c = isSynthetic();
        if (c && !n) {
            accessFlags = accessFlags - 0x1000;
        } else if (!c && n) {
            accessFlags = accessFlags + 0x1000;
        }
    }

    protected static PMethod read(DataInputStream in) throws IOException {
        int accessFlags = in.readUnsignedShort();
        int name_index = in.readUnsignedShort();
        int descriptor_index = in.readUnsignedShort();

        PMethod method = new PMethod(name_index, descriptor_index);

        int attribute_count = in.readUnsignedShort();

        for (int i = 0; i < attribute_count; i++) {
            method.attributes.add(new Attribute(in));
        }
        return method;
    }

    protected void write(DataOutputStream out) throws IOException {
        out.writeShort(accessFlags);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
        out.writeShort(attributes.size());
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = attributes.get(i);
            if (attr == null) continue;
            attr.write(out);
        }
    }
}
