package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CMethodHandle extends ConstantInfo {
	public CMethodHandle(ClassFile cf, int index) {
		super(CType.METHODHANDLE, cf, index);
	}
	
	public int reference_type = -1, reference_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		reference_type = in.read();
		reference_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "Method ndle";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		cf.getConstant(reference_index).from(s);
		return this;
	}
}
