package lava2;
import java.util.Hashtable;
import java.io.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.Type;

/**
* This loads classes and saves references to them.
*
* This loads the main method into pool index 0.
*
* External classes, fields and methods are just stored in the name.
*/

public class ClassLoader {
	public final static byte CONSTANT_Fieldref = (byte)9;
	Pool pool;
	boolean debug;

	public ClassLoader(Pool p,boolean debug) {
		pool = p;
		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	//the classFileName needs to end with a .class
	public void loadClass(String classFileName) throws IOException {
		log("loading class "+classFileName);
		ClassParser classp = new ClassParser(classFileName);
		JavaClass jclass = null;
		try {
			jclass = classp.parse();
		} catch(ClassFormatException x) {
			throw new IOException("ClassFormatException: "+x.getMessage());
		}
			//get the classname
		String jcname = jclass.getClassName();

		//class is the short name
		if (jcname == null) {log("jcname is null");}
		String claz = Pool.shortClassName(jcname);

		//load constantpool
		loadConstantPool(claz,jclass.getConstantPool());

		//load fields
		int cnx = jclass.getClassNameIndex();
		loadFields(claz,jclass,cnx);

		//load methods
		loadMethods(claz,jclass,cnx);
	}

	public void loadConstantPool(String claz,ConstantPool cpool) {
		String str = null;
		int class_index = 0;
		int natx = 0;
		String full_name = null;

		//start at 1
		for (int ix=1;ix<cpool.getLength();ix++) {
			Constant k = cpool.getConstant(ix);
			if (k==null) continue;
			byte tag=k.getTag();
			switch(tag) {
				case 3:		//int
					//store this as a class constant
					ConstantInteger ci = (ConstantInteger)k;
					int a = ci.getBytes();
					String sint = String.valueOf(a);
					pool.PUTS(claz,ix,sint);
					break;
				case 4:		//float
					ConstantFloat cf = (ConstantFloat)k;
					float f = cf.getBytes();
					String sfloat = String.valueOf(f);
					pool.PUTS(claz,ix,sfloat);
					break;
				case 7:		//class
					ConstantClass cc=(ConstantClass)k;
					str=cc.getBytes(cpool);
					pool.PUTS(claz,ix,str);
					break;
				case 8:		//string
					ConstantString cs=(ConstantString)k;
					str=cs.getBytes(cpool);
					pool.PUTS(claz,ix,str);
					break;
				case 9:		//field
					ConstantFieldref cfr = (ConstantFieldref)k;
					class_index = cfr.getClassIndex();
					natx = cfr.getNameAndTypeIndex();
					full_name = getFullName(cpool,class_index,natx);
					pool.PUTS(claz,ix,full_name);
					break;
				case 10:	//methodref
					ConstantMethodref cmr = (ConstantMethodref)k;
					class_index = cmr.getClassIndex();
					natx = cmr.getNameAndTypeIndex();
					full_name = getFullName(cpool,class_index,natx);
					pool.PUTS(claz,ix,full_name);
					break;
				case 11:	//ifacemethodref
				//store method name
					ConstantInterfaceMethodref cimr = (ConstantInterfaceMethodref)k;
					class_index = cimr.getClassIndex();
					natx = cimr.getNameAndTypeIndex();
					full_name = getFullName(cpool,class_index,natx);
					pool.PUTS(claz,ix,full_name);
					break;
				case 1: //log(ix+": utf8");
					break;
				case 12: //log(ix+": cnat");
					break;
				default: log(ix+": "+tag);
			}
		}
	}

	public String getFullName(ConstantPool cpool,int cx,int natx) {
		//get the class of the reference - it might be this one or could be different
		ConstantClass myClass=(ConstantClass)cpool.getConstant(cx);
		String cname=myClass.getBytes(cpool);
		ConstantNameAndType myCnat=(ConstantNameAndType)cpool.getConstant(natx);
		String fname=myCnat.getName(cpool);
		String fsig=myCnat.getSignature(cpool);
		String complete_name = cname + "." + fname + ":" + fsig;
		return complete_name;
	}

	//------------------------------------------
	//the fieldrefs have already been identified
	//but for the fields in this class we need to set the name
	public void loadFields(String claz,JavaClass jclass,int cnx) {
		Field[] fa = jclass.getFields();
		for (int i=0;i<fa.length;i++) {
			Field f = fa[i];
			//get the name
			String fname = f.getName();
			int fnx = f.getNameIndex();
			//get cpindex
			int cpx = getFieldPoolIndex(jclass.getConstantPool(),cnx, fname, fnx);
			if (cpx<0) {
				throw new IllegalStateException("no pool entry found for "+fname);
			}
			//store the name in the pool
			pool.putStaticName(claz,cpx, fname);
		}
	}

	/**
	* There should be an easier way of doing this.
	*/
	public int getFieldPoolIndex(ConstantPool cpool,int cnx,String fname,int fnx) {
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			if (k==null) continue;
			byte tag=k.getTag();
			if (tag==CONSTANT_Fieldref) {
				ConstantFieldref cfr = (ConstantFieldref)k;
				int cx = cfr.getClassIndex();
				int gnatx = cfr.getNameAndTypeIndex();
				ConstantNameAndType cnat = (ConstantNameAndType)cpool.getConstant(gnatx);
				int nx = cnat.getNameIndex();
				if (cx==cnx && nx==fnx) {
					return i;
				}
			}
		}
		log("no match found for "+fname);
		return -1;	//invalid
	}
	//-------------------------------------------

	public void loadMethods(String claz,JavaClass jclass,int cnx) {
		Method[] ma = jclass.getMethods();
		for (int i=0;i<ma.length;i++) {
			Method m = ma[i];
			//get the name
			String mname = m.getName();
			//log("ClassLoader.loadMethods: loading "+mname);
			int mnx = m.getNameIndex();
			//get cpindex
			int cpx = getMethodPoolIndex(jclass.getConstantPool(),cnx,mname, mnx);
			if (cpx<0) {
				//throw new IllegalStateException("no pool entry found for "+mname);
				//log("no pool entry found for "+mname);
				continue;
			}

			//store method name
			pool.putStaticName(claz,cpx, mname);
			//store method code
			int params = m.getArgumentTypes().length;
			//get the code
			byte[] mcode = m.getCode().getCode();
			String smcode = methodCode(params,mcode);
			pool.PUTS(claz,cpx,smcode);
		}
	}

	/**
	* I convert method code to a String for these reasons:
	*	1. A String is a universal object (in this version at least)
	*	2. A String is immutable
	*	3. I don't like dealing with negative numbers.
	*	4. At some point, I may have my own byte code.
	*/
	public static String methodCode(int params,byte[] mcode) {
		//System.out.println("ClassLoader.methodCode");
		char[] ca = new char[mcode.length+1];
		ca[0]=(char)params;
		for (int i=0;i<mcode.length;i++) {
			ca[i+1]=(char)(mcode[i] & 0xFF);
			//System.out.print("ca["+(i+1)+"]="+(int)ca[i+1]+" ");
		}
		return String.valueOf(ca);
	}

	/**
	* There should be an easier way of doing this.
	*	cnx is the classname index
	*	mnx is the method index
	*	We look through every method in the constantpool to find a MethodRef
	*	that matches.
	*/
	//cname is not needed
	public int getMethodPoolIndex(ConstantPool cpool,int cnx,String mname,int mnx) {
		//put the main method in slot 0
		if (mname.equals("main")) return 0;
		for (int i=1;i<cpool.getLength();i++) {
			Constant k = cpool.getConstant(i);
			byte tag=k.getTag();
			if (k!=null && (k instanceof ConstantMethodref)) {
				ConstantMethodref cmr = (ConstantMethodref)k;
				//we need to check the class because of <init>
				int cx = cmr.getClassIndex();
				String cname2 = cmr.getClass(cpool);
				int gnatx = cmr.getNameAndTypeIndex();
				ConstantNameAndType cnat = (ConstantNameAndType)cpool.getConstant(gnatx);
				int nx = cnat.getNameIndex();
				String mname2= cnat.getName(cpool);
				if (cx==cnx && nx==mnx) {
					return i;
				} else if (nx==mnx) {
					//method name matches but class doesn't
					//log("getMethodPoolIndex: found a match on '"+mname+"' but it is the wrong class ("+cx+", '"+cname2+"')");
				}
			}
		}
		//log("no match found for "+mname);
		return -1;	//invalid
	}
}