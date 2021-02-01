package lava2;
import java.io.IOException;

/**
* The control unit (CU) is a component of a computer's central processing unit (CPU) that directs the operation
* of the processor. It tells the computer's memory, arithmetic logic unit and input and output devices how to
* respond to the instructions that have been sent to the processor.
*/
public class ControlUnit implements OpCodes {
	Pool pool;
	Machine m;
	BlackBox box;
	ClassLoader cloader;
	boolean debug;
	boolean running=false;

	public ControlUnit(boolean debug) {

		this.debug=debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	public void start(String className,String[] args) throws IOException {
		pool = new Pool(256,debug);
		cloader = new ClassLoader(pool,debug);
		cloader.loadClass(className+".class");
		//if (debug) {
		//	log("ControlUnit: "+className+" pool is as follows:");
		//	pool.dump();
		//}
		box = new BlackBox(debug);
		m = new Machine(pool, box, debug);
		//load the main method
		m.MAIN_FRAME(className,args);
		running = true;
		run();
	}

	/**
	* given 2 chars, which hold byte values, combine them and return an unsigned int.
	*/
	public int index(char c1,char c2) {
		return c1 * 256 + c2;
	}

	/**
	* given 2 chars, which hold byte values, convert the first to signed, and combine them
	* and return a signed short
	*/
	public short shortIndex(char c1,char c2) {
		short s1 = (short)c1;
		if (c1>127) {
			s1 = (short)(s1 - 256);
		}
		return (short)(s1 * 256 + c2);
	}

	public char NEXT() { return box.NEXT();}

	public void run() {
		char op = (char)0;
		char index1=(char)0;
		char index2=(char)0;
		short sindex = (short)0;
		int idx = 0;
		int lev = 0;

		while (running) {
			//fetch the next byte
			op = NEXT();
			//log("ControlUnit: op ="+ (int)op);
			switch(op) {
				//load numbers on to stack
				case BIPUSH: index1=NEXT(); box.IPUSH((int)index1); break;
				case SIPUSH:
					index1=NEXT(); index2=NEXT(); sindex = shortIndex(index1,index2);
					box.IPUSH((int)sindex);
					break;
				case LDC: index1=NEXT(); m.LDC(index1); break;
				case ICONST_M1: box.IPUSH(-1); break;
				case ICONST_0: box.IPUSH(0); break;
				case ICONST_1: box.IPUSH(1); break;
				case ICONST_2: box.IPUSH(2); break;
				case ICONST_3: box.IPUSH(3); break;
				case ICONST_4: box.IPUSH(4); break;
				case ICONST_5: box.IPUSH(5); break;
				case DUP: box.DUP(); break;

				//math
				case IADD: m.IADD(); break;
				case ISUB: m.ISUB(); break;
				case IMUL: m.IMUL(); break;
				case IDIV: m.IDIV(); break;
				case IREM: m.IREM(); break;
				case IINC: index1=NEXT(); index2=NEXT(); m.IINC(index1,index2); break;

				//transfer data
				//load an int value from local variable 0
				case ILOAD: index1=NEXT(); m.ALOAD((int)index1); break;
				case ILOAD_0: m.ALOAD(0); break;
				case ILOAD_1: m.ALOAD(1); break;
				case ILOAD_2: m.ALOAD(2); break;
				case ILOAD_3: m.ALOAD(3); break;
				case ALOAD: index1=NEXT(); m.ALOAD((int)index1); break;
				case ALOAD_0: m.ALOAD(0); break;
				case ALOAD_1: m.ALOAD(1); break;
				case ALOAD_2: m.ALOAD(2); break;
				case ALOAD_3: m.ALOAD(3); break;
				case ISTORE: index1=NEXT(); m.ASTORE((int)index1); break;
				case ISTORE_0: m.ASTORE(0); break;
				case ISTORE_1: m.ASTORE(1); break;
				case ISTORE_2: m.ASTORE(2); break;
				case ISTORE_3: m.ASTORE(3); break;
				case ASTORE: index1=NEXT(); m.ASTORE((int)index1); break;
				case ASTORE_0: m.ASTORE(0); break;
				case ASTORE_1: m.ASTORE(1); break;
				case ASTORE_2: m.ASTORE(2); break;
				case ASTORE_3: m.ASTORE(3); break;

				case GETSTATIC:
					//log("ControlUnit: GETSTATIC called");
					index1=NEXT(); index2=NEXT(); idx = index(index1,index2);
					m.GETSTATIC(idx);
					break;
				case GETFIELD: index1=NEXT(); index2=NEXT(); m.GETFIELD( index(index1,index2)); break;
				case PUTSTATIC: index1=NEXT(); index2=NEXT(); m.PUTSTATIC( index(index1,index2)); break;
				case PUTFIELD: index1=NEXT(); index2=NEXT(); m.PUTFIELD( index(index1,index2)); break;

				//arrays
				case IALOAD: m.AALOAD(); break;
				case IASTORE: m.AASTORE(); break;
				case AALOAD: m.AALOAD(); break;
				case AASTORE: m.AASTORE(); break;
				case ARRAYLENGTH: m.ALEN(); break;
				case ANEWARRAY: index1=NEXT(); index2=NEXT(); m.ANEWARRAY( index(index1,index2)); break;
				case NEWARRAY: index1=NEXT(); m.NEWARRAY(index1); break;
				case NEWOBJ: index1=NEXT(); index2=NEXT(); m.NEWOBJ( index(index1,index2)); break;

				//control flow
				case JMP: index1=NEXT(); index2=NEXT(); m.IF(true, index(index1,index2) ); break;
				case IF_ICMPEQ: index1=NEXT(); index2=NEXT();  m.IF_ICMP("EQ",index(index1,index2) ); break;
				case IF_ICMPGE: index1=NEXT(); index2=NEXT();  m.IF_ICMP("GE",index(index1,index2) ); break;
				case IF_ICMPGT: index1=NEXT(); index2=NEXT();  m.IF_ICMP("GT",index(index1,index2) ); break;
				case IF_ICMPLE: index1=NEXT(); index2=NEXT();  m.IF_ICMP("LE",index(index1,index2) ); break;
				case IF_ICMPLT: index1=NEXT(); index2=NEXT();  m.IF_ICMP("LT",index(index1,index2) ); break;
				case IF_ICMPNE: index1=NEXT(); index2=NEXT();  m.IF_ICMP("NE",index(index1,index2) ); break;
				case IFEQ: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("EQ",index(index1,index2) ); break;
				case IFGE: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("GE",index(index1,index2) ); break;
				case IFGT: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("GT",index(index1,index2) ); break;
				case IFLE: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("LE",index(index1,index2) ); break;
				case IFLT: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("LT",index(index1,index2) ); break;
				case IFNE: index1=NEXT(); index2=NEXT(); m.IF_ICMPZ("NE",index(index1,index2) ); break;
				case IFNULL: index1=NEXT(); index2=NEXT(); m.IFNULL( index(index1,index2)); break;

				//subroutines
				case RETURNV:
					//log("RETURNV");
					lev = m.RETURNV();
					//log ("lev="+lev);
					if (lev==0) { log("PROGRAM COMPLETE"); running = false;}
					break;
				case IRETURN: m.ARETURN(); break;
				case ARETURN: m.ARETURN(); break;
				case INVOKESTATIC:
				case INVOKEVIRTUAL:
				case INVOKESPECIAL:  index1=NEXT(); index2=NEXT();  m.INVOKE( index(index1,index2)); break;

				//other
				case CHECKCAST: index1=NEXT(); index2=NEXT(); m.CHECKCAST( index(index1,index2)); break;

				default:
					log("unknown op "+op+" (0x"+Integer.toHexString(op)+")");
			}
		}
	}
}