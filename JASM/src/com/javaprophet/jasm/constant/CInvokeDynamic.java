package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CInvokeDynamic extends ConstantInfo {
	public CInvokeDynamic(ClassFile cf, int index) {
		super(CType.INVOKEDYNAMIC, cf, index);
	}
	
	public int bootstrap_method_attr_index = -1, name_and_type_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		bootstrap_method_attr_index = in.readUnsignedShort();
		name_and_type_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "InvokeDynamic";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		return null;
	}
}
