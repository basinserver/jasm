package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

public class CNameAndType extends CConstant<Pair<Integer, Integer>> {
	public CNameAndType(int name_ref, int descriptor_ref) {
		super(Pair.of(name_ref, descriptor_ref), 12);
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value.left);
		out.writeShort(this.value.right);
	}
}
