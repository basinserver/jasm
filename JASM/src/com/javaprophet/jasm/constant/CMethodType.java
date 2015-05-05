package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CMethodType extends ConstantInfo {
	public CMethodType(ClassFile cf, int index) {
		super(CType.METHODTYPE, cf, index);
	}
	
	public int descriptor_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		descriptor_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "MethodType";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		cf.getConstant(descriptor_index).from(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeShort(descriptor_index);
		return this;
	}
}
