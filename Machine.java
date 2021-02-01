package lava2;

/**
* The Machine has all the commands, but not the control loop.
*/
public class Machine {
	Pool pool;
	BlackBox box;
	boolean debug;

	public Machine(Pool p,BlackBox b, boolean d) {
		pool=p;
		box = b;
		debug = d;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	//----------------------------------------
	//do comparison first and get it out of the way
	public static int compareCode(String compare) {
		if (compare==null) return 0;
		switch (compare) {
			case "EQ":
			case "==": return 2;
			case "NE":
			case "NEQ":
			case "!=": return 5;
			case "LT":
			case "<": return 4;
			case "LE":
			case "LTE":
			case "<=": return 6;
			case "GT":
			case ">": return 1;
			case "GE":
			case "GTE":
			case ">=": return 3;
			default: return 0;
		}
	}

	//code must be a number from 0..6 as given above
	public static boolean ICMP(int a, int code,int b) {
		//System.out.println("ICMP: a="+a+", code="+code+", b="+b);
		int c = a - b;
		int t = 0;
		if (c>0) t=1;
		else if (c==0) t=2;
		//c must be less than 0, but no need to check
		else t=4;

		int d = t & code;
		return d > 0;
	}

	public void IFNULL(int rel) {
		String a = box.POP();
		if (a==null || a.equals("NULL")) {
			IF(true,rel);
		}
	}

	//do a relative branch
	//do the adjustment here
	//This is also used for a straight GOTO, just pass it a TRUE
	public void IF(boolean b,int rel) {
		if (b) box.JUMP(rel - 3);
	}

	//cmp is the comparison.  Use "EQ" or other similar symbols
	public void IF_ICMP(String cmp,int relativeJump) {
		//System.out.println("IF_ICMP: cmp="+cmp);
		int b = box.IPOP();
		int a = box.IPOP();
		int cc = compareCode(cmp);
		IF( ICMP(a,cc,b), relativeJump);
	}

	//same as ICMP but you compare to zero
	public void IF_ICMPZ(String cmp,int relativeJump) {
		int b = 0;
		int a = box.IPOP();
		int cc = compareCode(cmp);
		IF( ICMP(a,cc,b), relativeJump);
	}

	//----------------------------------------------
	//push a byte on to the stack
	//test this with negative numbers
	//public void BIPUSH(char b) {
	//	box.IPUSH((int)b);
	//}

	//public void SIPUSH(short s) {
	//	box.IPUSH((int)s);
	//}

	public void LDC(char index) {
		String claz = box.getClaz();
		box.PUSH(pool.GETS(claz,(int)index));
	}

	public void IADD() {
		int b = box.IPOP();
		int a = box.IPOP();
		box.IPUSH(a + b);
	}

	public void ISUB() {
		int b = box.IPOP();
		int a = box.IPOP();
		box.IPUSH(a - b);
	}

	public void IMUL() {
		int b = box.IPOP();
		int a = box.IPOP();
		box.IPUSH(a * b);
	}

	public void IDIV() {
		int b = box.IPOP();
		int a = box.IPOP();
		box.IPUSH(a / b);
	}

	public void IREM() {
		int b = box.IPOP();
		int a = box.IPOP();
		box.IPUSH(a % b);
	}

	public void INEG() {
		int a = box.IPOP();
		box.IPUSH( 0 - a);
	}

	public void IINC(char localn,char k) {
		if (localn<0 || localn>7) throw new IllegalStateException("local variable "+localn+" requested");
		try {
			String sint = box.LOAD(localn);
			int i = Integer.parseInt(sint);
			i = i + k;
			box.STORE(localn,String.valueOf(i));
		} catch (Exception x) {
			log(x.getMessage());
		}
	}

	//load a reference onto the stack from local variable 0
	public void ALOAD(int localn) {
		//log("aload "+localn);
		String data = box.LOAD(localn);
		box.PUSH( data );
		//log("pushing "+data+" on to stack");
	}

	//store a reference into local variable 0
	public void ASTORE(int localn) {
		//log("astore "+localn);
		box.STORE( localn, box.POP());
	}

	public void GETSTATIC(int index) {
		String claz = box.getClaz();
		//log("Machine.GETSTATIC: claz = "+claz+"; index = "+index);
		String ref = pool.GETS(claz,index);
		//log("Machine.GETSTATIC: ref = "+ref);
		box.PUSH( ref );
	}

	public void PUTSTATIC(int index) {
		String claz = box.getClaz();
		pool.PUTS(claz,index, box.POP());
	}

	public void GETFIELD(int index) {
		String oref = box.POP();
		box.PUSH( pool.GETF(oref,index));
	}

	public void PUTFIELD(int index) {
		String oref = box.POP();
		pool.PUTF(oref,index, box.POP());
	}

	//arrayref, index -> value
	//this also works with IALOAD
	public void AALOAD() {
		int index = box.IPOP();
		String aref = box.POP();
		//log("Machine.AALOAD: aref = "+aref+"; index = "+index);
		String val = pool.AALD(aref,index);
		//log("Machine.AALOAD: pushing '"+val+"' on to stack");
		box.PUSH(val);
	}

	//arrayref, index, value -> nil
	//store a value into an array
	public void AASTORE() {
		String val = box.POP();
		int index = box.IPOP();
		String aref = box.POP();
		pool.AAST(aref,index,val);
	}

	public void ALEN() {
		String aref = box.POP();
		box.IPUSH( pool.ALEN(aref));
	}

	public void ANEWARRAY(int index) {
		//get the classname of the new array. It is stored in the constant pool
		String claz = box.getClaz();
		String arrayClass = pool.GETS(claz,index);
		int count = box.IPOP();
		//create a new array
		String aref = pool.ANAR(arrayClass,count);
		box.PUSH(aref);

	}

	//count -> arrayref	create new array with count elements of primitive type identified by atype
	public void NEWARRAY(char atype) {
		int count = box.IPOP();
		String aref = pool.NUAR( (int)atype,count);
		box.PUSH(aref);
	}

	public void NEWOBJ(int index) {
		String claz = box.getClaz();
		String objClass = pool.GETS(claz,index);
		String oref = pool.NUOB(objClass);
		box.PUSH(oref);
	}

	//returns the current level
	//if this is 0 then the program is completed
	public int RETURNV() {
		return box.RETURNV();
	}

	//return a value.  Also works with IRETURN
	public int ARETURN() {
		return box.ARETURN();
	}

	/**
	* invoke_static, invoke_virtual, and invoke_special all operate the same way.
	* The only difficulty is determining if we have the code for it or if we emulate it.
	* If we have the method name, then we have the code.
	*
	* This only invokes a method in the same class, so this needs some work
	*/
	public void INVOKE(int index) {
		String claz = box.getClaz();
		String mname = pool.getStaticName(claz,index);
		String mcode = pool.GETS(claz,index);
		if (mname!=null && mcode!=null) {
			box.INVOKE(claz,mname,mcode);
		} else {
			//log("unable to invoke "+mcode);
			//we don't have the code so fake it
			Native.emulate(mcode,box,pool,debug);
		}
	}

	//this has the code to start the main frame
	//the args are stored in a string array and loaded into param1
	public void MAIN_FRAME(String className,String[] args) {
		//log("Machine.MAIN_FRAME: className="+className+"; args.length="+args.length);
		//if (args!=null && args.length > 0 ) {
		if (args!=null) {
			//create a new array
			String aref = pool.ANAR("String",args.length);
			//store the strings in it
			for (int i=0;i<args.length;i++) {
				pool.AAST(aref,i,args[i]);
			}
			//put it on the stack
			//log("Machine.MAIN_FRAME: pushing '"+aref+"' to data stack");
			box.PUSH(aref);
			//it will be taken off the stack and put into a param later
		} else {
			String aref = pool.ANAR("String",0);
			box.PUSH(aref);
		}
		//can't use INVOKE here because claz is not set yet
		String claz = Pool.shortClassName(className);
		//the code is in 0
		String mname = pool.getStaticName(claz,0);
		String mcode = pool.GETS(claz,0);
		if (mname!=null && mcode!=null) {
			box.INVOKE(claz,mname,mcode);
		} else {
			//to do
			log("unable to invoke "+mcode);
		}
	}

	/**
	* This is a NOP because we don't do anything. But we do look at the classes.
	*/
	public void CHECKCAST(int index) {
		String oref = box.POP();
		//what is the class of this object? It's in the ref itself
		String oclass = oref.substring(0, oref.indexOf((int)'$'));
		//now what class do we think it is?
		String claz = box.getClaz();
		String classToCheck = pool.GETS(claz,index);
		log("CHECKCAST: object is of type "+oclass+", checking to see if it is of type "+classToCheck);
		box.PUSH(oref);
	}
}