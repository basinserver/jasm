package com.javaprophet.jasm.field;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.attribute.AttributeInfo;

public class FieldInfo {
	public FieldInfo() {
		
	}
	
	private int accessFlags = -1;
	public int name_index = -1, descriptor_index = -1;
	public AttributeInfo[] ai = null;
	
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
	
	public boolean isVolatile() {
		return (accessFlags & 0x0040) == 0x0040;
	}
	
	public void setVolatile(boolean n) {
		boolean c = isVolatile();
		if (c && !n) {
			accessFlags = accessFlags - 0x0040;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0040;
		}
	}
	
	public boolean isTransient() {
		return (accessFlags & 0x0080) == 0x0080;
	}
	
	public void setTransient(boolean n) {
		boolean c = isTransient();
		if (c && !n) {
			accessFlags = accessFlags - 0x0080;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0080;
		}
	}
	
	public boolean isSynthetic() {
		return (accessFlags & 0x1000) == 0x1000;
	}
	
	public void setSynthtic(boolean n) {
		boolean c = isSynthetic();
		if (c && !n) {
			accessFlags = accessFlags - 0x1000;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x1000;
		}
	}
	
	public boolean isEnum() {
		return (accessFlags & 0x4000) == 0x4000;
	}
	
	public void setEnum(boolean n) {
		boolean c = isEnum();
		if (c && !n) {
			accessFlags = accessFlags - 0x4000;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x4000;
		}
	}
	
	// TODO: attribute support more
	
	public FieldInfo read(DataInputStream in) throws IOException {
		accessFlags = in.readUnsignedShort();
		name_index = in.readUnsignedShort();
		descriptor_index = in.readUnsignedShort();
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			ai[i] = new AttributeInfo().read(in);
		}
		return this;
	}
	
	public FieldInfo write(DataOutputStream out) throws IOException {
		out.writeShort(accessFlags);
		out.writeShort(name_index);
		out.writeShort(descriptor_index);
		out.writeShort(ai.length);
		for (int i = 0; i < ai.length; i++) {
			ai[i].write(out);
		}
		return this;
	}
}
