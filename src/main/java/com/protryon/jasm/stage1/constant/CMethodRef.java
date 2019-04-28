package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

public class CMethodRef extends CConstant<Pair<Integer, Integer>> {
	public CMethodRef(int class_ref, int name_and_type_ref) {
		super(Pair.of(class_ref, name_and_type_ref), 10);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value.left);
		out.writeShort(this.value.right);
	}
}
