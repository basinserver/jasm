package com.javaprophet.jasm.constant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class CFieldRef extends ConstantInfo {
	public CFieldRef(ClassFile cf, int index) {
		super(CType.FIELDREF, cf, index);
	}
	
	public int class_index = -1, name_and_type_index = -1;
	
	@Override
	public ConstantInfo read(DataInputStream in) throws IOException {
		class_index = in.readUnsignedShort();
		name_and_type_index = in.readUnsignedShort();
		return this;
	}
	
	@Override
	public String getName() {
		return "FieldRef";
	}
	
	@Override
	public ConstantInfo from(String s) throws Exception {
		if (!s.contains(".")) throw new Exception("Malformed Field Reference!");
		cf.getConstant(class_index).from(s.substring(0, s.indexOf('.')));
		cf.getConstant(name_and_type_index).from(s.substring(s.indexOf('.') + 1));
		return this;
	}
	
	@Override
	public ConstantInfo write(DataOutputStream out) throws IOException {
		out.writeShort(class_index);
		out.writeShort(name_and_type_index);
		return this;
	}
}
