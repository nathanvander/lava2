package lava2;

import java.io.IOException;

public class Lava {
	public final static int version=2;

	public static void main(String[] args) throws IOException {
		System.out.println("Lava version "+version);
		String classname = args[0];
		String[] args2=null;
		if (args.length>1) {
			args2 = new String[args.length-1];
			System.arraycopy(args,1,args2,0,args2.length);
		}
		ControlUnit control = new ControlUnit(true);
		control.start(classname, args2);
	}
}