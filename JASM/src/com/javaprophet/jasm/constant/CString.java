package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CString extends ConstantInfo {
	public CString(ClassFile cf, int index) {
		super(CType.STRING, cf, index);
	}
	
	public int string_index;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		string_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "String";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		cf.getConstant(string_index).from(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeShort(string_index);
		return this;
	}
}
