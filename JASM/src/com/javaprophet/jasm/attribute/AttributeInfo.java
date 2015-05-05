package com.javaprophet.jasm.attribute;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AttributeInfo {
	public int attribute_name_index = -1;
	public byte[] attribute_info = null;
	
	public AttributeInfo read(DataInputStream in) throws IOException {
		return read(in.readUnsignedShort(), in);
	}
	
	public AttributeInfo read(int name_index, DataInputStream in) throws IOException {
		attribute_name_index = name_index;
		byte[] aib = new byte[in.readInt()];
		in.readFully(aib);
		attribute_info = aib;
		return this;
	}
	
	public AttributeInfo write(DataOutputStream out) throws IOException {
		out.writeShort(attribute_name_index);
		out.writeInt(attribute_info.length);
		out.write(attribute_info);
		return this;
	}
	
}
