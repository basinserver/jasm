package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CLong extends ConstantInfo {
	public CLong(ClassFile cf, int index) {
		super(cf, index);
	}
	
	public long lng = -1L;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		lng = in.readLong();
		return this;
	}
	
	@Override
	public String getName() {
		return "Long";
	}
	
	@Override
	public String toString() {
		return lng + "";
	}
	
}
