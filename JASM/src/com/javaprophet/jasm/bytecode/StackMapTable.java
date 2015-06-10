package com.javaprophet.jasm.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.javaprophet.jasm.ClassFile;

public class StackMapTable {
	private final ClassFile cf;
	private final Code code;
	public int name_index = -1;
	
	public StackMapTable(Code code, ClassFile cf) {
		this.cf = cf;
		this.code = code;
	}
	
	private static VerifyType readType(DataInputStream in) throws IOException {
		int i = in.read();
		if (i == 0) {
			return VerifyType.TOP;
		}else if (i == 1) {
			return VerifyType.INTEGER;
		}else if (i == 2) {
			return VerifyType.FLOAT;
		}else if (i == 3) {
			return VerifyType.DOUBLE;
		}else if (i == 4) {
			return VerifyType.LONG;
		}else if (i == 5) {
			return VerifyType.NULL;
		}else if (i == 6) {
			return VerifyType.UNINITIALZEDTHIS;
		}else if (i == 7) {
			return new VerifyTypeObject(in.readUnsignedShort());
		}else if (i == 8) {
			return new VerifyTypeUninitializedVariable(in.readUnsignedShort());
		}
		return null;
	}
	
	private static void writeType(DataOutputStream out, VerifyType type) throws IOException {
		out.write(type.i);
		if (type.i < 7) {
			// nothing else
		}else if (type.i == 7) {
			out.writeShort(((VerifyTypeObject)type).cpool_index);
		}else if (type.i == 8) {
			out.writeShort(((VerifyTypeUninitializedVariable)type).offset);
		}
	}
	
	public StackMapEntry[] ents = null;
	
	public StackMapTable read(int name_index, DataInputStream in) throws IOException {
		this.name_index = name_index;
		in.readInt();
		ents = new StackMapEntry[in.readUnsignedShort()];
		for (int i = 0; i < ents.length; i++) {
			int ft = in.read();
			StackMapEntry sf = new StackMapEntry();
			sf.frame_type = ft;
			if (ft < 64) {
				sf.calc_offset = ft;
			}else if (ft < 128) {
				sf.calc_offset = ft - 64;
				sf.vti = new VerifyType[]{readType(in)};
			}else if (ft < 247) {
				// invalid!!!
			}else if (ft == 247) {
				int off = in.readUnsignedShort();
				sf.calc_offset = off;
				sf.vti = new VerifyType[]{readType(in)};
			}else if (ft < 251) {
				int off = in.readUnsignedShort();
				sf.calc_offset = off;
			}else if (ft == 251) {
				int off = in.readUnsignedShort();
				sf.calc_offset = off;
			}else if (ft < 255) {
				int off = in.readUnsignedShort();
				sf.calc_offset = off;
				int l = ft - 251;
				sf.vti = new VerifyType[l];
				for (int i2 = 0; i2 < l; i2++) {
					sf.vti[i2] = readType(in);
				}
			}else if (ft == 255) {
				int off = in.readUnsignedShort();
				sf.calc_offset = off;
				int nol = in.readUnsignedShort();
				sf.vti = new VerifyType[nol];
				for (int i2 = 0; i2 < nol; i2++) {
					sf.vti[i2] = readType(in);
				}
				int si = in.readUnsignedShort();
				sf.vti2 = new VerifyType[si];
				for (int i2 = 0; i2 < si; i2++) {
					sf.vti2[i2] = readType(in);
				}
			}
			ents[i] = sf;
		}
		for (int i = 0; i < ents.length; i++) {
			System.out.println(ents[i].frame_type + ", " + ents[i].calc_offset);
			if (ents[i].vti != null) for (VerifyType vt : ents[i].vti) {
				System.out.println("verify : " + vt.i);
			}
			if (ents[i].vti2 != null) for (VerifyType vt : ents[i].vti2) {
				System.out.println("verify2 : " + vt.i);
			}
		}
		return this;
	}
	
	public StackMapTable write(DataOutputStream out) throws IOException {
		out.writeShort(name_index);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out2 = new DataOutputStream(buf);
		if (ents != null && ents.length > 0) for (StackMapEntry sme : ents) {
			if (sme != null) {
				out2.write(sme.frame_type);
				if (sme.frame_type < 64) {
					// nothing
		}else if (sme.frame_type < 128) {
			if (sme.vti != null && sme.vti.length >= 1) {
				writeType(out2, sme.vti[0]);
			}
		}else if (sme.frame_type < 247) {
			// invalid!!!
		}else if (sme.frame_type == 247) {
			out2.writeShort(sme.calc_offset);
			if (sme.vti != null && sme.vti.length >= 1) {
				writeType(out2, sme.vti[0]);
			}
		}else if (sme.frame_type < 251) {
			out2.writeShort(sme.calc_offset);
		}else if (sme.frame_type == 251) {
			out2.writeShort(sme.calc_offset);
		}else if (sme.frame_type < 255) {
			out2.writeShort(sme.calc_offset);
			if (sme.vti != null) for (int i = 0; i < sme.vti.length; i++) {
				writeType(out2, sme.vti[i]);
			}
		}else if (sme.frame_type == 255) {
			out2.writeShort(sme.calc_offset);
			if (sme.vti != null) {
				out2.writeShort(sme.vti.length);
				for (int i = 0; i < sme.vti.length; i++) {
					writeType(out2, sme.vti[i]);
				}
			}
			if (sme.vti2 != null) {
				out2.writeShort(sme.vti2.length);
				for (int i = 0; i < sme.vti2.length; i++) {
					writeType(out2, sme.vti2[i]);
				}
			}
		}
	}
}
		return this;
	}
}
