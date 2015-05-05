package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CFloat extends ConstantInfo {
	public CFloat(ClassFile cf, int index) {
		super(CType.FLOAT, cf, index);
	}
	
	public float flt = -1F;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		flt = in.readFloat();
		return this;
	}
	
	@Override
	public String getName() {
		return "Float";
	}
	
	@Override
	public String toString() {
		return flt + "";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		flt = Float.parseFloat(s);
		return this;
	}
}
