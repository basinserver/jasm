package com.javaprophet.jasm.field;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.attribute.AttributeInfo;

public class FieldInfo {
	public FieldInfo() {
		
	}
	
	public int access_flags = -1, name_index = -1, descriptor_index = -1;
	public AttributeInfo[] ai = null;
	
	public FieldInfo read(DataInputStream in) throws IOException {
		access_flags = in.readUnsignedShort();
		name_index = in.readUnsignedShort();
		descriptor_index = in.readUnsignedShort();
		int ac = in.readUnsignedShort();
		ai = new AttributeInfo[ac];
		for (int i = 0; i < ai.length; i++) {
			ai[i] = new AttributeInfo().read(in);
		}
		return this;
	}
	
	public FieldInfo write(DataOutputStream out) throws IOException {
		out.writeShort(access_flags);
		out.writeShort(name_index);
		out.writeShort(descriptor_index);
		out.writeShort(ai.length);
		for (int i = 0; i < ai.length; i++) {
			ai[i].write(out);
		}
		return this;
	}
}
