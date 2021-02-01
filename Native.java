package lava2;

/**
* This emulates native code.
*/
public class Native {
	public final static String PARSEINT="java/lang/Integer.parseInt:(Ljava/lang/String;)I";
	public final static String PRINTLN_I="java/io/PrintStream.println:(I)V";
	public final static String PRINTLN="java/io/PrintStream.println:(Ljava/lang/String;)V";
	public final static String SB_INIT="java/lang/StringBuilder.<init>:()V";
	public final static String SB_APPEND="java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;";
	public final static String SB_APPEND_I="java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;";
	public final static String SB2S="java/lang/StringBuilder.toString:()Ljava/lang/String;";

	public static void log(boolean debug,String s) {
		if (debug) System.out.println(s);
	}

	public static boolean emulate(String methodName,BlackBox box,Pool pool,boolean debug) {
		if (methodName.equals(PARSEINT)) {
			PARSEINT(box,pool);
			return true;
		} else if (methodName.equals(PRINTLN_I)) {
			PRINTLN_I(box,pool);
			return true;
		} else if (methodName.equals(PRINTLN)) {
			PRINTLN(box,pool);
			return true;
		} else if (methodName.equals(SB_INIT)) {
			SB_INIT(box,pool);
			return true;
		} else if (methodName.equals(SB_APPEND)) {
			SB_APPEND(box,pool);
			return true;
		} else if (methodName.equals(SB_APPEND_I)) {
			SB_APPEND_I(box,pool);
			return true;
		} else if (methodName.equals(SB2S)) {
			SB2S(box,pool);
			return true;
		} else {
			log(debug,"unable to emulate '"+methodName+"'");
			return false;
		}
	}

	//this is the emulation of static
	//"java/lang/Integer.parseInt:(Ljava/lang/String;)I"
	//public static void static_Integer_parseInt_String_I(BlackBox box,BHeap bheap) {
	public static void PARSEINT(BlackBox box,Pool pool) {
		//log(true, "emulating PARSEINT");
		//log(true,box.dumpStack());
		String str = box.POP();
		//----------------------------------
		//this is the native code
		int i=java.lang.Integer.parseInt(str);
		//----------------------------------
		box.IPUSH(i);
	}

	//"java/io/PrintStream.println:(I)V";
	public static void PRINTLN_I (BlackBox box,Pool pool) {
		int ival = box.IPOP();			//int ref
		String oref = box.POP();		//java.lang.System.out:PrintStream object
		//----------------------------------
		//this is the native code
		System.out.println(ival);
		//----------------------------------
	}

	//this is the emulation of "java/io/PrintStream.println:(Ljava/lang/String;)V"
	public static void PRINTLN(BlackBox box,Pool pool) {
		String sval = box.POP();		//string
		String oref = box.POP();		//java.lang.System.out:PrintStream object
		//----------------------------------
		//this is the native code
		System.out.println(sval);
		//----------------------------------
	}


	//the java code shows that this creates a AbstractStringBuilder with an initial capacity of 16
	//since I don't want to resize it, I will make the capacity 32
	//java/lang/StringBuilder.<init>:()V
	public static void SB_INIT(BlackBox box,Pool pool) {
		String oref = box.POP();
		//log(true,"StringBuilder_init: oref="+oref.toString());
		//create an array to store the dynamic string in
		//these are bytes but it doesn't really matter
		String aref = pool.NUAR(8,64);
		//log(true,"StringBuilder_init: created new array "+aref);
		//now where do we store the array ref?
		//it doesn't matter as long as we are consistent
		//how about index #1
		pool.AAST(oref,1,aref);
		//heap.arrayStore(oref,1,aref.toInt());
		//we just consumed an oref and we don't need to put anything back on the stack
		//let's store the pointer in #2, but since it is zero we don't need to do anything
		//pool.dump();
	}


	//
	//StringBuilder.append:(Ljava/lang/String;)
	public static void SB_APPEND(BlackBox box,Pool pool) {
		//pop the string off the stack
		String sval = box.POP();
		//get the stringbuilder object
		String oref = box.POP();
		//get the aref, which is in slot 1 of the stringbuilder object
		String aref = pool.AALD(oref,1);
		//get the array pointer
		String aptr = pool.AALD(oref,2);
		int iptr = 0;
		if (aptr!=null) {
			iptr = Integer.parseInt(aptr);
		}
		//get the bytes from the string
		byte[] str = sval.getBytes();
		//now here is the fun part. store these as ints in our embedded array
		for (int i=0;i<str.length;i++) {
			int x = i + iptr;
			//int c = (int)str[i];
			//log("storing "+c);
			//heap.arrayStore(aref,x,str[i]);
			pool.AAST(aref,x,String.valueOf((int)str[i]));
		}
		iptr=iptr+str.length;
		//save the pointer
		//log("StringBuilder_append_String ptr="+ptr);
		//heap.arrayStore(oref,2,ptr);
		pool.AAST(oref,2,String.valueOf(iptr));
		//return the oref
		box.PUSH(oref);
	}

	//java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
	public static void SB_APPEND_I(BlackBox box,Pool pool) {
		//get the int. It is already in string form
		String snum = box.POP();
		//the oref is the stringbuilder object
		String oref = box.POP();
		//log("StringBuilder_append_int: oref="+oref.toString());
		String aref = pool.AALD(oref,1);
		//get the array pointer initially it is 0
		String aptr = pool.AALD(oref,2);
		int iptr = 0;
		if (aptr!=null) {
			iptr = Integer.parseInt(aptr);
		}
		byte[] bnum = snum.getBytes();
		for (int i=0;i<bnum.length;i++) {
			int x = i + iptr;
			//heap.arrayStore(aref,x,(int)bnum[i]);
			pool.AAST(aref,x,String.valueOf((int)bnum[i]));
		}
		iptr=iptr+bnum.length;
		//save the pointer
		//log("StringBuilder_append_int ptr="+ptr);
		//heap.arrayStore(oref,2,ptr);
		pool.AAST(oref,2,String.valueOf(iptr));
		//return the oref
		box.PUSH(oref);
	}

	//java/lang/StringBuilder.toString:()Ljava/lang/String;
	public static void SB2S(BlackBox box,Pool pool) {
		String oref = box.POP();
		String aref = pool.AALD(oref,1);
		//Word aref = new Word(heap.arrayLoad(oref,1));

		//get the ptr
		String aptr = pool.AALD(oref,2);
		int iptr = 0;
		if (aptr!=null) {
			iptr = Integer.parseInt(aptr);
		}

		//copy it into a new byte array
		byte[] str = new byte[iptr];
		for (int i=0;i<iptr;i++) {
			String item = pool.AALD(aref,i);
			//str[i]=(byte)pool.AALD(aref,i);
			str[i] = (byte)Integer.parseInt(item);
		}
		//now save the string
		//log ("StringBuilder_toString: str="+new String(str));
		//Word sref=heap.storeAscii(Word.ASCII,str);
		String sref = new String(str);
		//return it
		box.PUSH(sref);
	}
}
/**
	//have fun with this
	//"java/lang/Object.<init>:()V"
	public void Object_init() {
		Word oref = POP();
		//what is the object class? it is located in slot 0
		int k = heap.arrayLoad(oref,0);
		Word wk = new Word(k);
		String sk = new String( heap.loadAscii(wk));
		log("calling Object.<init> from object "+oref.toString()+" which has class "+wk+" ("+sk+")");
		log("and God blessed his child "+oref.toString()+" from the tribe of "+sk+" and told him to live long and prosper");
		//return void

	}
*/