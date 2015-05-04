package com.javaprophet.jasm;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
import com.javaprophet.jasm.constant.CUTF8;
import com.javaprophet.jasm.constant.ConstantInfo;
import com.javaprophet.jasm.field.FieldInfo;
import com.javaprophet.jasm.method.MethodInfo;

public class ClassFile {
	public ClassFile(File f) throws IOException {
		this(new FileInputStream(f)); // TODO: ensure closed
	}
	
	public ClassFile(byte[] ba) throws IOException {
		this(new ByteArrayInputStream(ba));
	}
	
	private final DataInputStream in;
	
	public ClassFile(InputStream in) throws IOException {
		this.in = new DataInputStream(in);
		read();
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
	
	public String resolveConstant(int cref) {
		return (String)resolveConstant(cref, true);
	}
	
	public String resolveConstant(int cref, boolean resolve) {
		return (String)resolveConstant(cref, resolve, true);
	}
	
	private Object resolveConstant(int cref, boolean resolve, boolean base) {
		ConstantInfo ci = getConstant(cref);
		Object res = null;
		if (ci instanceof CClass) {
			res = resolveConstant(((CClass)ci).name_index, false);
		}else if (ci instanceof CDouble) {
			res = ((CDouble)ci).dbl + "";
		}else if (ci instanceof CLong) {
			res = ((CLong)ci).lng + "";
		}else if (ci instanceof CInteger) {
			res = ((CInteger)ci).integer + "";
		}else if (ci instanceof CFloat) {
			res = ((CFloat)ci).flt + "";
		}else if (ci instanceof CFieldRef) {
			res = resolveConstant(((CFieldRef)ci).class_index, false) + "/" + (String)resolveConstant(((CFieldRef)ci).name_and_type_index, false);
		}else if (ci instanceof CMethodRef) {
			res = resolveConstant(((CMethodRef)ci).class_index, false) + "/" + (String)resolveConstant(((CMethodRef)ci).name_and_type_index, false);
		}else if (ci instanceof CInterfaceMethodRef) {
			res = resolveConstant(((CInterfaceMethodRef)ci).class_index, false) + "/" + (String)resolveConstant(((CInterfaceMethodRef)ci).name_and_type_index, false);
		}else if (ci instanceof CInvokeDynamic) {
			res = resolveConstant(((CInvokeDynamic)ci).name_and_type_index, false);
		}else if (ci instanceof CMethodHandle) {
			res = resolveConstant(((CMethodHandle)ci).reference_index, false); // TODO: reference_type
		}else if (ci instanceof CMethodType) {
			res = resolveConstant(((CMethodType)ci).descriptor_index, false);
		}else if (ci instanceof CNameAndType) {
			res = resolveConstant(((CNameAndType)ci).name_index, false) + " " + (String)resolveConstant(((CNameAndType)ci).descriptor_index, false); // TODO: reference_type
		}else if (ci instanceof CString) {
			res = resolveConstant(((CString)ci).string_index, false);
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
	private ConstantInfo[] ci = null;
	private FieldInfo[] fi = null;
	private MethodInfo[] mi = null;
	private AttributeInfo[] ai = null;
	private int[] is = null;
	private int thisClass = -1, superClass = -1, accessFlags = -1;
	
	public void read() throws IOException {
		if (in.read() != 0x000000CA || in.read() != 0x000000FE || in.read() != 0x000000BA || in.read() != 0x000000BE) {
			throw new IOException("Not a Class File! Magic is not 0xCAFEBABE.");
		}
		int minor = in.readUnsignedShort();
		int major = in.readUnsignedShort();
		this.version = major + "." + minor;
		int cpc = in.readUnsignedShort();
		this.ci = new ConstantInfo[cpc];
		for (int i = 1; i < this.ci.length; i++) {
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
				break;
			case 6:
				this.ci[i] = new CDouble(this, i).read(in);
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
			}
		}
		accessFlags = in.readUnsignedShort();
		thisClass = in.readUnsignedShort();
		superClass = in.readUnsignedShort();
		int ifc = in.readUnsignedShort();
		this.is = new int[ifc];
		for (int i = 0; i < ifc; i++) {
			this.is[i] = in.readUnsignedShort(); // TODO: ?
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
	}
	
	public void close() throws IOException {
		in.close();
	}
}
