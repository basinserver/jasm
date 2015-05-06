package com.javaprophet.jasm.method;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;
import com.javaprophet.jasm.attribute.AttributeInfo;
import com.javaprophet.jasm.bytecode.Code;
import com.javaprophet.jasm.constant.CUTF8;

public class MethodInfo {
	private final ClassFile cf;
	
	public MethodInfo(ClassFile cf) {
		this.cf = cf;
	}
	
	public int getAccessFlags() {
		return accessFlags;
	}
	
	public void setAccessFlags(int access_flags) {
		this.accessFlags = access_flags;
	}
	
	public boolean isPublic() {
		return (accessFlags & 0x0001) == 0x0001;
	}
	
	public void setPublic(boolean n) {
		boolean c = isPublic();
		if (c && !n) {
			accessFlags = accessFlags - 0x0001;
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
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
		}else if (!c && n) {
			accessFlags = accessFlags + 0x1000;
		}
	}
	
	private int accessFlags = -1;
	public int name_index = -1, descriptor_index = -1;
	public AttributeInfo[] ai = null;
	public Code code = null;
	
	public ClassFile getParent() {
		return cf;
	}
	
	public MethodInfo read(DataInputStream in) throws IOException {
		accessFlags = in.readUnsignedShort();
		name_index = in.readUnsignedShort();
		String mname = ((CUTF8)cf.getConstant(name_index)).utf;
		
		descriptor_index = in.readUnsignedShort();
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			int name_index = in.readUnsignedShort();
			String name = ((CUTF8)cf.getConstant(name_index)).utf;
			if (name.equals("Code")) {
				code = new Code(mname, cf).read(name_index, in);
			}else {
				ai[i] = new AttributeInfo().read(name_index, in);
			}
		}
		return this;
	}
	
	public MethodInfo write(DataOutputStream out) throws IOException {
		out.writeShort(accessFlags);
		out.writeShort(name_index);
		out.writeShort(descriptor_index);
		out.writeShort(ai.length);
		if (code != null) {
			code.write(out);
		}
		for (int i = 0; i < ai.length; i++) {
			if (ai[i] == null) continue;
			ai[i].write(out);
		}
		return this;
	}
	
	public String getName() {
		return ((CUTF8)cf.getConstant(name_index)).utf;
	}
	
	public String toString() {
		return getName();
	}
}
