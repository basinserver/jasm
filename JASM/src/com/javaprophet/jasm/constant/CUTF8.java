package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;

public class CUTF8 extends ConstantInfo {
	public String utf = null;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		byte[] utf = new byte[in.readUnsignedShort()];
		in.readFully(utf);
		this.utf = new String(utf);
		return this;
	}
	
}
