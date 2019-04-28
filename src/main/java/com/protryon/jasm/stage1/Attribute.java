package com.protryon.jasm.stage1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Attribute {
	public final int attribute_name_index;
	public final byte[] attribute_info;

	protected Attribute(DataInputStream in) throws IOException {
		this(in.readUnsignedShort(), in);
	}

	protected Attribute(int name_index, DataInputStream in) throws IOException {
		attribute_name_index = name_index;
		byte[] aib = new byte[in.readInt()];
		in.readFully(aib);
		attribute_info = aib;
	}

	public Attribute(int name_index, byte[] info) {
		this.attribute_name_index = name_index;
		this.attribute_info = info;
	}
	
	protected Attribute write(DataOutputStream out) throws IOException {
		out.writeShort(attribute_name_index);
		out.writeInt(attribute_info.length);
		out.write(attribute_info);
		return this;
	}
	
}
