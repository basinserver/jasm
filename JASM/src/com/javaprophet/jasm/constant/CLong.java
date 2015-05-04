package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CLong extends ConstantInfo {
	public long lng = -1L;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		lng = in.readLong();
		return this;
	}
	
}
