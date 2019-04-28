package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

public class CMethodHandle extends CConstant<Pair<Byte, Integer>> {
	public CMethodHandle(byte type, int reference_index) {
		super(Pair.of(type, reference_index), 15);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.write(this.value.left);
		out.writeShort(this.value.right);
	}
}
