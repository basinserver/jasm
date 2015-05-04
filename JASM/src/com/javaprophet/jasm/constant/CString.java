package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CString extends ConstantInfo {
	public CString(ClassFile cf, int index) {
		super(cf, index);
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
}
