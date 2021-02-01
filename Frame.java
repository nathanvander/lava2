package lava2;

public class Frame {
	int level = 0;
	String claz;	//4 character name
	String methodName;
	String code;
	//instruction pointer for the method
	//it always points to the next code to be executed
	int IP = 1;

	public Frame() {}

	public Frame(int lev,String c,String m,String code) {
		level = lev;
		claz = c;
		methodName = m;
		this.code = code;
	}

	public String getClaz() {return claz;}
	public String getMethodName() {return methodName;}

	public int params() {return (int)code.charAt(0);}

	public char NEXT() {return code.charAt(IP++);}

	//this is a relative jump.  The caller is responsible for adjusting the
	//offset, which I believe is -3
	public void JUMP(int rel) {
		IP = IP + rel;
	}

	public String dump() {
		StringBuffer sb = new StringBuffer("========");
		sb.append("level="+level);
		sb.append("class="+claz);
		sb.append("method="+methodName);
		sb.append("params="+params());
		sb.append("I="+IP);
		return sb.toString();
	}

	protected void finalize() {
		System.out.println("frame #"+level+": "+methodName+" is complete");
	}
}