package com.javaprophet.jasm;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.javaprophet.jasm.attribute.AttributeInfo;
import com.javaprophet.jasm.constant.CClass;
import com.javaprophet.jasm.constant.CDouble;
import com.javaprophet.jasm.constant.CFieldRef;
import com.javaprophet.jasm.constant.CFloat;
import com.javaprophet.jasm.constant.CInteger;
import com.javaprophet.jasm.constant.CInterfaceMethodRef;
import com.javaprophet.jasm.constant.CInvokeDynamic;
import com.javaprophet.jasm.constant.CLong;
import com.javaprophet.jasm.constant.CMethodHandle;
import com.javaprophet.jasm.constant.CMethodRef;
import com.javaprophet.jasm.constant.CMethodType;
import com.javaprophet.jasm.constant.CNameAndType;
import com.javaprophet.jasm.constant.CString;
import com.javaprophet.jasm.constant.CType;
import com.javaprophet.jasm.constant.CUTF8;
import com.javaprophet.jasm.constant.ConstantInfo;
import com.javaprophet.jasm.field.FieldInfo;
import com.javaprophet.jasm.method.MethodInfo;

public class ClassFile {
	public ClassFile() {
		
	}
	
	public ClassFile(File f) throws IOException {
		this(new FileInputStream(f)); // TODO: ensure closed
	}
	
	public ClassFile(byte[] ba) throws IOException {
		this(new ByteArrayInputStream(ba));
	}
	
	public ClassFile(InputStream in) throws IOException {
		read(new DataInputStream(in));
	}
	
	public String getVersion() {
		return version;
	}
	
	public ConstantInfo[] getConstantPool() {
		return ci;
	}
	
	public FieldInfo[] getFieldPool() {
		return fi;
	}
	
	public FieldInfo getField(int i) {
		return fi[i];
	}
	
	public MethodInfo[] getMethodPool() {
		return mi;
	}
	
	public MethodInfo getMethod(int i) {
		return mi[i];
	}
	
	public AttributeInfo[] getAttributePool() {
		return ai;
	}
	
	public AttributeInfo getAttribute(int i) {
		return ai[i];
	}
	
	public ConstantInfo getConstant(int i) {
		return ci[i];
	}
	
	public void setConstant(int i, ConstantInfo info) {
		ci[i] = info;
	}
	
	public int getClassIndex() {
		return thisClass;
	}
	
	public int getSuperClassIndex() {
		return superClass;
	}
	
	public int getAccessFlags() {
		return accessFlags;
	}
	
	public String toString() {
		return getClassName();
	}
	
	private static final String crlf = System.getProperty("line.separator");
	
	public ConstantInfo reconstuctConstant(ConstantInfo parent, int index, String s) throws Exception {
		switch (parent.type) {
		case CLASS:
			CClass ccls = (CClass)new CClass(this, index);
			ccls.name_index = ((CClass)parent).name_index;
			return ccls.from(s);
		case DOUBLE:
			CDouble cdb = (CDouble)new CDouble(this, index);
			cdb.dbl = ((CDouble)parent).dbl;
			return cdb.from(s);
		case FIELDREF:
			CFieldRef cfr = (CFieldRef)new CFieldRef(this, index);
			cfr.class_index = ((CFieldRef)parent).class_index;
			cfr.name_and_type_index = ((CFieldRef)parent).name_and_type_index;
			return cfr.from(s);
		case FLOAT:
			CFloat cf = (CFloat)new CFloat(this, index);
			cf.flt = ((CFloat)parent).flt;
			return cf.from(s);
		case INTEGER:
			CInteger cint = (CInteger)new CInteger(this, index);
			cint.integer = ((CInteger)parent).integer;
			return cint.from(s);
		case INTERFACEMETHODREF:
			CInterfaceMethodRef cimr = (CInterfaceMethodRef)new CInterfaceMethodRef(this, index);
			cimr.class_index = ((CInterfaceMethodRef)parent).class_index;
			cimr.name_and_type_index = ((CInterfaceMethodRef)parent).name_and_type_index;
			return cimr.from(s);
		case INVOKEDYNAMIC:
			CInvokeDynamic cid = (CInvokeDynamic)new CInvokeDynamic(this, index);
			cid.bootstrap_method_attr_index = ((CInvokeDynamic)parent).bootstrap_method_attr_index;
			cid.name_and_type_index = ((CInvokeDynamic)parent).name_and_type_index;
			return cid.from(s);
		case LONG:
			CLong cl = (CLong)new CLong(this, index);
			cl.lng = ((CLong)parent).lng;
			return cl.from(s);
		case METHODHANDLE:
			CMethodHandle cml = (CMethodHandle)new CMethodHandle(this, index);
			cml.reference_index = ((CMethodHandle)parent).reference_index;
			cml.reference_type = ((CMethodHandle)parent).reference_type;
			return cml.from(s);
		case METHODREF:
			CMethodRef cmr = (CMethodRef)new CMethodRef(this, index);
			cmr.class_index = ((CMethodRef)parent).class_index;
			cmr.name_and_type_index = ((CMethodRef)parent).name_and_type_index;
			return cmr.from(s);
		case METHODTYPE:
			CMethodType cmt = (CMethodType)new CMethodType(this, index);
			cmt.descriptor_index = ((CMethodType)parent).descriptor_index;
			return cmt.from(s);
		case NAMEANDTYPE:
			CNameAndType cnat = (CNameAndType)new CNameAndType(this, index);
			cnat.name_index = ((CNameAndType)parent).name_index;
			cnat.descriptor_index = ((CNameAndType)parent).descriptor_index;
			return cnat.from(s);
		case STRING:
			CString cs = (CString)new CString(this, index);
			cs.string_index = ((CString)parent).string_index;
			return cs.from(s);
		case UTF8:
			CUTF8 cu = (CUTF8)new CUTF8(this, index);
			cu.utf = ((CUTF8)parent).utf;
			return cu.from(s);
		default:
			throw new Exception("Invalid Type!");
		}
	}
	
	public String resolveConstant(int cref) {
		return (String)resolveConstant(cref, true);
	}
	
	public String resolveConstant(int cref, boolean resolve) {
		return (String)resolveConstant(cref, resolve, true, false);
	}
	
	private Object resolveConstant(int cref, boolean resolve, boolean base, boolean sw) {
		ConstantInfo ci = getConstant(cref);
		Object res = null;
		if (ci instanceof CClass) {
			res = resolveConstant(((CClass)ci).name_index, resolve, false, false);
		}else if (ci instanceof CDouble) {
			res = ((CDouble)ci).dbl + "";
		}else if (ci instanceof CLong) {
			res = ((CLong)ci).lng + "";
		}else if (ci instanceof CInteger) {
			res = ((CInteger)ci).integer + "";
		}else if (ci instanceof CFloat) {
			res = ((CFloat)ci).flt + "";
		}else if (ci instanceof CFieldRef) {
			res = resolveConstant(((CFieldRef)ci).class_index, resolve, false, false) + "/" + (String)resolveConstant(((CFieldRef)ci).name_and_type_index, resolve, false, true);
		}else if (ci instanceof CMethodRef) {
			res = resolveConstant(((CMethodRef)ci).class_index, resolve, false, false) + "/" + (String)resolveConstant(((CMethodRef)ci).name_and_type_index, resolve, false, false);
		}else if (ci instanceof CInterfaceMethodRef) {
			res = resolveConstant(((CInterfaceMethodRef)ci).class_index, resolve, false, false) + "/" + (String)resolveConstant(((CInterfaceMethodRef)ci).name_and_type_index, resolve, false, false);
		}else if (ci instanceof CInvokeDynamic) {
			res = resolveConstant(((CInvokeDynamic)ci).name_and_type_index, resolve, false, false);
		}else if (ci instanceof CMethodHandle) {
			res = resolveConstant(((CMethodHandle)ci).reference_index, resolve, false, false); // TODO: reference_type
		}else if (ci instanceof CMethodType) {
			res = resolveConstant(((CMethodType)ci).descriptor_index, resolve, false, false);
		}else if (ci instanceof CNameAndType) {
			res = resolveConstant(((CNameAndType)ci).name_index, resolve, false, false) + (sw ? " " : "\0") + (String)resolveConstant(((CNameAndType)ci).descriptor_index, resolve, false, false); // TODO: reference_type
		}else if (ci instanceof CString) {
			res = resolveConstant(((CString)ci).string_index, resolve, false, false);
		}else if (ci instanceof CUTF8) {
			res = ((CUTF8)ci).utf;
		}
		if (resolve && base) {
			String ress = (String)res;
			ress = cref + " // -> " + ress;
			ress = ress.replace(crlf, crlf + "//");
			res = ress;
		}
		return res;
	}
	
	public String getClassName() {
		CClass pp = (CClass)ci[thisClass];
		CUTF8 name = (CUTF8)ci[pp.name_index];
		return name.utf;
	}
	
	public String getSuperClassName() {
		CClass pp = (CClass)ci[superClass];
		CUTF8 name = (CUTF8)ci[pp.name_index];
		return name.utf;
	}
	
	private String version = null;
	private int minorVersion = -1, majorVersion = -1;
	private ConstantInfo[] ci = null;
	private FieldInfo[] fi = null;
	private MethodInfo[] mi = null;
	private AttributeInfo[] ai = null;
	private int[] is = null;
	private int thisClass = -1, superClass = -1, accessFlags = -1;
	
	public void read(DataInputStream in) throws IOException {
		if (in.read() != 0x000000CA || in.read() != 0x000000FE || in.read() != 0x000000BA || in.read() != 0x000000BE) {
			throw new IOException("Not a Class File! Magic is not 0xCAFEBABE.");
		}
		minorVersion = in.readUnsignedShort();
		majorVersion = in.readUnsignedShort();
		this.version = majorVersion + "." + minorVersion;
		int cpc = in.readUnsignedShort();
		this.ci = new ConstantInfo[cpc];
		for (int i = 1; i < this.ci.length; i++) {
			if (i == 49) {
				System.nanoTime();
			}
			int type = in.read();
			switch (type) {
			case 1:
				this.ci[i] = new CUTF8(this, i).read(in);
				break;
			case 3:
				this.ci[i] = new CInteger(this, i).read(in);
				break;
			case 4:
				this.ci[i] = new CFloat(this, i).read(in);
				break;
			case 5:
				this.ci[i] = new CLong(this, i).read(in);
				i++;
				this.ci[i] = this.ci[i - 1];
				break;
			case 6:
				this.ci[i] = new CDouble(this, i).read(in);
				i++;
				this.ci[i] = this.ci[i - 1];
				break;
			case 7:
				this.ci[i] = new CClass(this, i).read(in);
				break;
			case 8:
				this.ci[i] = new CString(this, i).read(in);
				break;
			case 9:
				this.ci[i] = new CFieldRef(this, i).read(in);
				break;
			case 10:
				this.ci[i] = new CMethodRef(this, i).read(in);
				break;
			case 11:
				this.ci[i] = new CInterfaceMethodRef(this, i).read(in);
				break;
			case 12:
				this.ci[i] = new CNameAndType(this, i).read(in);
				break;
			case 15:
				this.ci[i] = new CMethodHandle(this, i).read(in);
				break;
			case 16:
				this.ci[i] = new CMethodType(this, i).read(in);
				break;
			case 18:
				this.ci[i] = new CInvokeDynamic(this, i).read(in);
				break;
			default:
				break;
			}
		}
		accessFlags = in.readUnsignedShort();
		thisClass = in.readUnsignedShort();
		superClass = in.readUnsignedShort();
		int ifc = in.readUnsignedShort();
		this.is = new int[ifc];
		for (int i = 0; i < ifc; i++) {
			this.is[i] = in.readUnsignedShort();
		}
		int fc = in.readUnsignedShort();
		this.fi = new FieldInfo[fc];
		for (int i = 0; i < this.fi.length; i++) {
			this.fi[i] = new FieldInfo().read(in);
		}
		int mc = in.readUnsignedShort();
		this.mi = new MethodInfo[mc];
		for (int i = 0; i < mc; i++) {
			this.mi[i] = new MethodInfo(this).read(in);
		}
		int ac = in.readUnsignedShort();
		this.ai = new AttributeInfo[ac];
		for (int i = 0; i < ac; i++) {
			this.ai[i] = new AttributeInfo().read(in);
		}
		in.close();
	}
	
	public boolean isPublic() {
		return (accessFlags & 0x0001) == 0x0001;
	}
	
	public void setPublic(boolean n) {
		boolean c = isPublic();
		if (c && !n) {
			accessFlags = accessFlags - 0x0001;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0001;
		}
	}
	
	public boolean isFinal() {
		return (accessFlags & 0x0010) == 0x0010;
	}
	
	public void setFinal(boolean n) {
		boolean c = isFinal();
		if (c && !n) {
			accessFlags = accessFlags - 0x0010;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0010;
		}
	}
	
	public boolean isSuper() {
		return (accessFlags & 0x0020) == 0x0020;
	}
	
	public void setSuper(boolean n) {
		boolean c = isSuper();
		if (c && !n) {
			accessFlags = accessFlags - 0x0020;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0020;
		}
	}
	
	public boolean isInterface() {
		return (accessFlags & 0x0200) == 0x0200;
	}
	
	public void setInterface(boolean n) {
		boolean c = isInterface();
		if (c && !n) {
			accessFlags = accessFlags - 0x0200;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0200;
		}
	}
	
	public boolean isAbstract() {
		return (accessFlags & 0x0400) == 0x0400;
	}
	
	public void setAbstract(boolean n) {
		boolean c = isAbstract();
		if (c && !n) {
			accessFlags = accessFlags - 0x0400;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x0400;
		}
	}
	
	public boolean isSynthetic() {
		return (accessFlags & 0x1000) == 0x1000;
	}
	
	public void setSynthetic(boolean n) {
		boolean c = isSynthetic();
		if (c && !n) {
			accessFlags = accessFlags - 0x1000;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x1000;
		}
	}
	
	public boolean isAnnotation() {
		return (accessFlags & 0x2000) == 0x2000;
	}
	
	public void setAnnotation(boolean n) {
		boolean c = isAnnotation();
		if (c && !n) {
			accessFlags = accessFlags - 0x2000;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x2000;
		}
	}
	
	public boolean isEnum() {
		return (accessFlags & 0x4000) == 0x4000;
	}
	
	public void setEnum(boolean n) {
		boolean c = isEnum();
		if (c && !n) {
			accessFlags = accessFlags - 0x4000;
		}else if (!c && n) {
			accessFlags = accessFlags + 0x4000;
		}
	}
	
	public void setAccessFlags(int accessFlags) {
		this.accessFlags = accessFlags;
	}
	
	public void write(DataOutputStream out) throws IOException {
		out.write(0x000000CA);
		out.write(0x000000FE);
		out.write(0x000000BA);
		out.write(0x000000BE);
		out.writeShort(minorVersion);
		out.writeShort(majorVersion);
		out.writeShort(ci.length);
		boolean lb = false;
		for (int i = 1; i < ci.length; i++) {
			if (ci[i].type == CType.LONG || ci[i].type == CType.DOUBLE) {
				if (!lb) {
					lb = true;
				}else {
					lb = false;
					continue;
				}
			}
			switch (ci[i].type) {
			case CLASS:
				out.write(7);
				break;
			case DOUBLE:
				out.write(6);
				break;
			case FIELDREF:
				out.write(9);
				break;
			case FLOAT:
				out.write(4);
				break;
			case INTEGER:
				out.write(3);
				break;
			case INTERFACEMETHODREF:
				out.write(11);
				break;
			case INVOKEDYNAMIC:
				out.write(18);
				break;
			case LONG:
				out.write(5);
				break;
			case METHODHANDLE:
				out.write(15);
				break;
			case METHODREF:
				out.write(10);
				break;
			case METHODTYPE:
				out.write(16);
				break;
			case NAMEANDTYPE:
				out.write(12);
				break;
			case STRING:
				out.write(8);
				break;
			case UTF8:
				out.write(1);
				break;
			default:
				break;
			}
			ci[i].write(out);
		}
		out.writeShort(accessFlags);
		out.writeShort(thisClass);
		out.writeShort(superClass);
		out.writeShort(is.length);
		for (int i = 0; i < is.length; i++) {
			out.writeShort(is[i]);
		}
		out.writeShort(fi.length);
		for (int i = 0; i < this.fi.length; i++) {
			fi[i].write(out);
		}
		out.writeShort(mi.length);
		for (int i = 0; i < mi.length; i++) {
			mi[i].write(out);
		}
		out.writeShort(ai.length);
		for (int i = 0; i < ai.length; i++) {
			ai[i].write(out);
		}
		out.close();
	}
}
