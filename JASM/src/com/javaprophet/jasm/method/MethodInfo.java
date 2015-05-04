package com.javaprophet.jasm.method;

import java.io.DataInputStream;
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
	
	public int access_flags = -1, name_index = -1, descriptor_index = -1;
	public AttributeInfo[] ai = null;
	public Code code = null;
	
	public ClassFile getParent() {
		return cf;
	}
	
	public MethodInfo read(DataInputStream in) throws IOException {
		access_flags = in.readUnsignedShort();
		name_index = in.readUnsignedShort();
		String mname = ((CUTF8)cf.getConstant(name_index)).utf;
		
		descriptor_index = in.readUnsignedShort();
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			int name_index = in.readUnsignedShort();
			String name = ((CUTF8)cf.getConstant(name_index)).utf;
			if (name.equals("Code")) {
				code = new Code(mname, cf).read(in);
			}else {
				ai[i] = new AttributeInfo().read(name_index, in);
			}
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
