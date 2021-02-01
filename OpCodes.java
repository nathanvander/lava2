package lava2;

public interface OpCodes {

	public final static char NOP = (char)0;

	//Group 1: Logic
	public final static char ISHL = (char)0x78;			//120
	public final static char ISHR = (char)0x7A;			//122
	public final static char IUSHR =(char)0x7C;			//124
	public final static char IAND = (char)0x7E;			//126
	public final static char IOR =  (char)0x80;			//128
	public final static char IXOR = (char)0x82;			//130

	//Group 2: Math
	public final static char IADD = (char)0x60;			//96
	public final static char ISUB = (char)0x64;			//100
	public final static char IMUL = (char)0x68;			//104
	public final static char IDIV = (char)0x6c;			//108
	public final static char IREM = (char)0x70;			//112
	public final static char INEG = (char)0x74;			//116
	public final static char IINC = (char)0x84;			//132

	//Group 3: Store/Load
	public final static char BIPUSH = (char)0x10; 		//decimal 16
	public final static char SIPUSH = (char)0x11;			//17
	public final static char LDC = (char)0x12;			//14
	public final static char POP = (char)0x57;			//87
	public final static char ILOAD = (char)0x15;		//26
	public final static char ILOAD_0 = (char)0x1A;		//26
	public final static char ILOAD_1 = (char)0x1B;		//27
	public final static char ILOAD_2 = (char)0x1C;		//28
	public final static char ILOAD_3 = (char)0x1D;		//29
	public final static char ALOAD = (char)0x19;		//42
	public final static char ALOAD_0 = (char)0x2A;		//42
	public final static char ALOAD_1 = (char)0x2B;
	public final static char ALOAD_2 = (char)0x2C;
	public final static char ALOAD_3 = (char)0x2D;
	public final static char ISTORE = (char)0x36;		//59
	public final static char ISTORE_0 = (char)0x3B;		//59
	public final static char ISTORE_1 = (char)0x3C;		//60
	public final static char ISTORE_2 = (char)0x3D;		//61
	public final static char ISTORE_3 = (char)0x3E;		//62
	public final static char ASTORE = (char)0x3A;
	public final static char ASTORE_0 = (char)0x4B;		//75?
	public final static char ASTORE_1 = (char)0x4C;
	public final static char ASTORE_2 = (char)0x4D;
	public final static char ASTORE_3 = (char)0x4E;
	public final static char GETSTATIC = (char)0xB2;	//178
	public final static char GETFIELD = (char)0xB4;	//178
	public final static char PUTSTATIC  = (char)0xB3;	//179
	public final static char PUTFIELD  = (char)0xB5;	//179
	//public final static char CALOAD = (char)0x34;		//52
	//public final static char CASTORE = (char)0x55;		//85
	//leave the constants the same
	public final static char ICONST_M1 = (char)0x2;
	public final static char ICONST_0 = (char)0x3;
	public final static char ICONST_1 = (char)0x4;
	public final static char ICONST_2 = (char)0x5;
	public final static char ICONST_3 = (char)0x6;
	public final static char ICONST_4 = (char)0x7;
	public final static char ICONST_5 = (char)0x8;
	public final static char LCONST_0 = (char)0x9;
	public final static char LCONST_1 = (char)0xA;
	public final static char DUP = (char)0x59;

	//Group 3A: arrays
	public final static char IALOAD = (char)0x2E;
	public final static char IASTORE = (char)0x4F;
	public final static char AALOAD = (char)0x32;
	public final static char AASTORE = (char)0x53;
	public final static char ARRAYLENGTH = (char)0xBE;
	public final static char ANEWARRAY = (char)0xBD;
	public final static char NEWARRAY = (char)0xBC;
	public final static char NEWOBJ = (char)0xBB;		//aka new

	//Group 4: Control Flow
	public final static char JMP = (char)0xA7;			//167 same as GOTO
	public final static char IF_ACMPEQ = (char)0xA5;
	public final static char IF_ICMPEQ = (char)0x9F;	//159
	public final static char IF_ICMPGE = (char)0xA2; 	//162
	public final static char IF_ICMPGT = (char)0xA3; 	//163
	public final static char IF_ICMPLE = (char)0xA4; 	//164
	public final static char IF_ICMPLT = (char)0xA1; 	//165
	public final static char IF_ICMPNE = (char)0xA0; 	//160
	public final static char IFEQ = (char)0x99;			//153
	public final static char IFGE = (char)0x9C;			//156
	public final static char IFGT = (char)0x9D;			//157
	public final static char IFLE = (char)0x9E;			//158
	public final static char IFLT = (char)0x9B;			//155
	public final static char IFNE = (char)0x9A;			//154
	public final static char IFNONNULL = (char)0xC7;
	public final static char IFNULL = (char)0xC6;

	//Group 5: subroutines
	public final static char RETURNV = (char)0xB1;		//177 aka RETURN
	public final static char IRETURN = (char)0xAC;		//172 return an int from method
	public final static char ARETURN = (char)0xB0;
	public final static char INVOKESTATIC = (char)0xB8;			//184
	public final static char INVOKEVIRTUAL = (char)0xB6;			//184
	public final static char INVOKESPECIAL = (char)0xB7;

	//Group 6: other
	public final static char CHECKCAST = (char)0xC0;
}