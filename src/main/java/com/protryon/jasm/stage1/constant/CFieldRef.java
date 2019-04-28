package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

public class CFieldRef extends CConstant<Pair<Integer, Integer>> {
	public CFieldRef(int class_index, int name_and_type_index) {
		super(Pair.of(class_index, name_and_type_index), 9);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value.left);
		out.writeShort(this.value.right);
	}
}
