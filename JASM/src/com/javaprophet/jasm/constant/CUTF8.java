package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CUTF8 extends ConstantInfo {
	public CUTF8(ClassFile cf, int index) {
		super(CType.UTF8, cf, index);
	}
	
	public String utf = null;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		byte[] utf = new byte[in.readUnsignedShort()];
		in.readFully(utf);
		this.utf = new String(utf);
		return this;
	}
	
	@Override
	public String getName() {
		return "UTF8";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		this.utf = cf.inscape(s);
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		byte[] utf = this.utf.getBytes();
		out.writeShort(utf.length);
		out.write(utf);
		return this;
	}
}
