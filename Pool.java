package lava2;
import java.util.*;

/**
* The Pool is our internal flat-file database.  This takes care
* of everything involving classes, objects and arrays.
*/
public class Pool {
	HashMap<String,String> map;
	int counter=1;
	boolean debug;

	public Pool(int size,boolean debug) {
		map = new HashMap<String,String>(size);
		this.debug = debug;
	}

	public void log(String s) {
		if (debug) System.out.println(s);
	}

	//return the first 4 characters in upper case
	public static String shortClassName(String className) {
		if (className==null) {
			throw new IllegalArgumentException("className is null");
		}
		//just get the last part
		if (className.contains(".")) {
			//System.out.println("Pool.shortClassName: className = "+className);
			//ugh .. regex
			String[] sa = className.split("\\.");
			//System.out.println("Pool.shortClassName: sa.length = "+sa.length);
			className = sa[sa.length-1];
		}
		if (className.contains("/")) {
			String[] sa = className.split("\\/");
			//System.out.println("Pool.shortClassName: sa.length = "+sa.length);
			className = sa[sa.length-1];
		}
		if (className.length()>4) {
			className = className.substring(0,4);
		}
		return className.toUpperCase();
	}

	//The atype operand of each newarray instruction must take one of the values T_BOOLEAN (4),
	//T_CHAR (5), T_FLOAT (6), T_DOUBLE (7), T_BYTE (8), T_SHORT (9), T_INT (10), or
	//T_LONG (11).
	//given the type, return a 4 character string type
	public static String atype(int atype) {
		switch(atype) {
			case 4: return "BOOL";
			case 5: return "CHAR";
			case 6: return "FLOA";
			case 7: return "DOUB";
			case 8: return "BYTE";
			case 9: return "SHOR";
			case 11: return "LONG";
			default: return "";		//int is the default
		}
	}

	//create new array with count elements of primitive type identified by atype
	//return array ref
	public String NUAR(int t,int count) {
		String aref = "[" + atype(t) + "$" + String.valueOf(counter++);
		map.put(aref + "~length", String.valueOf(count));
		return aref;
	}

	//anewarray
	//return array ref
	public String ANAR(String claz,int count) {
		String aref = "[" + shortClassName(claz) + "$" + String.valueOf(counter++);
		map.put(aref + "~length", String.valueOf(count));
		map.put(aref + "~type", claz);
		return aref;
	}

	//return object ref
	public String NUOB(String claz) {
		String oref = shortClassName(claz) + "$" + String.valueOf(counter++);
		map.put(oref + "~class", claz);
		log("created new object "+oref+" of class "+claz);
		return oref;
	}

	//arraylength
	//return -1 if not found
	public int ALEN(String aref) {
		String key = aref + "~length";
		String slen = map.get(key);
		if (slen==null) {
			return -1;
		} else {
			return Integer.parseInt(slen);
		}
	}

	//arrayref, index => value
	//AALOAD
	public String AALD(String aref, int index) {
		return map.get(aref + "#" + index);
	}

	public void AAST(String aref,int index,String value) {
		map.put(aref + "#" + index,value);
	}

	//return an int from an array
	public int IALD(String aref, int index) {
		String si = map.get(aref + "#" + index);
		return Integer.parseInt(si);
	}

	public void IAST(String aref,int index,int value) {
		map.put(aref + "#" + index,String.valueOf(value));
	}

	//getfield
	public String GETF(String oref,int index) {
		return map.get(oref + "#" + index);
	}

	//getstatic
	public String GETS(String className,int index) {
		String key = shortClassName(className) + "#" + index;
		return map.get(key);
	}

	//putfield
	public void PUTF(String oref,int index,String v) {
		map.put(oref + "#" + index, v);
	}

	//putstatic
	public void PUTS(String className,int index, String v) {
		String key = shortClassName(className) + "#" + index;
		map.put(key, v);
	}

	//save the name of the static field. Only used by ClassLoader
	public void putStaticName(String className,int index, String name) {
		String key = shortClassName(className) + "#" + index + "~name";
		map.put(key, name);
	}

	//this might return null
	public String getStaticName(String className,int index) {
		String key = shortClassName(className) + "#" + index + "~name";
		return map.get(key);
	}

	public void dump() {
        map.forEach((k, v) -> {
			if (!isAscii(v)) {
				System.out.println(k+" = [code]");
			} else {
            	System.out.println(k+" = "+v);
			}
        });
	}

	public static boolean isAscii(String s) {
		for (int i = 0;i<s.length(); i++) {
			char c = s.charAt(i);
			if (c>126) return false;
		}
		return true;
	}

	//======================================
	public static void main(String[] args) {
		Pool p = new Pool(16,true);
		String aref = p.NUAR(10,10);
		System.out.println(aref);

		p.dump();
	}
}