package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CClass extends ConstantInfo {
	public CClass(ClassFile cf, int index) {
		super(CType.CLASS, cf, index);
	}
	
	public int name_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		name_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "Class";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		cf.getConstant(name_index).from(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeShort(name_index);
		return this;
	}
	
}
