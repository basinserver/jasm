package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CNameAndType extends ConstantInfo {
	public CNameAndType(ClassFile cf, int index) {
		super(CType.NAMEANDTYPE, cf, index);
	}
	
	public int name_index = -1, descriptor_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		name_index = in.readUnsignedShort();
		descriptor_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "NameType";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		if (s.contains("\0")) {
			cf.getConstant(name_index).from(s.substring(0, s.indexOf("\0")));
			cf.getConstant(descriptor_index).from(s.substring(s.indexOf("\0") + 1));
		}else {
			cf.getConstant(name_index).from(s.substring(0, s.indexOf(" ")));
			cf.getConstant(descriptor_index).from(s.substring(s.indexOf(" ") + 1));
		}
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeShort(name_index);
		out.writeShort(descriptor_index);
		return this;
	}
}
