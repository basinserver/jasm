package com.protryon.jasm.stage1.constant;

import com.protryon.jasm.stage1.CConstant;
import com.shapesecurity.functional.Pair;

import java.io.DataOutputStream;
import java.io.IOException;

public class CDynamic extends CConstant<Pair<Integer, Integer>> {
	public CDynamic(int bootstrap_method_attr_index, int name_and_type_index) {
		super(Pair.of(bootstrap_method_attr_index, name_and_type_index), 17);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeShort(this.value.left);
		out.writeShort(this.value.right);
	}
}
