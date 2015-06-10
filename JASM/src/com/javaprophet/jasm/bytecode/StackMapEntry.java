package com.javaprophet.jasm.bytecode;

public class StackMapEntry {
	public int frame_type = -1;// 1 byte
	public int calc_offset = 0; // sometimes NOT read/written, sometimes is. Probably should not be changed.
	public VerifyType[] vti = null; // null/length 0 for none
	public VerifyType[] vti2 = null; // only used for full_frame
}
