package com.protryon.jasm.stage1.constant;

import java.io.DataOutputStream;
import java.io.IOException;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

public class CInvokeDynamic extends CConstant<Pair<Integer, Integer>> {
	public CInvokeDynamic(int bootstrap_method_attr_index, int name_and_type_index) {
		super(Pair.of(bootstrap_method_attr_index, name_and_type_index), 18);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value.left);
		out.writeShort(this.value.right);
	}
}
