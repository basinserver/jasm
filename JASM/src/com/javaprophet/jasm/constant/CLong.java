package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CLong extends ConstantInfo {
	public CLong(ClassFile cf, int index) {
		super(CType.LONG, cf, index);
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
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		this.lng = Long.parseLong(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeLong(lng);
		return this;
	}
}
