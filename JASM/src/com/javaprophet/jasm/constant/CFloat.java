package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CFloat extends ConstantInfo {
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
	
}
