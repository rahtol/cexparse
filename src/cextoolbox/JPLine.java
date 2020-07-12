package cextoolbox;
import java.io.PrintStream;


public class JPLine implements Comparable<JPLine> {
	
	public int flag0;
	public int flag1;
	public int flag2;
	public int offset;
	public int count0;
	public int count1;
	public static String covermode = "C1"; // "C1" or "C2" are legal values, anything different interpreted as C1 coverage
	public boolean startOfStmt;
	public int len; // code length in bytes, calculated by subtracting offset of consecutive plines; always zero for last pline in bline
	
	static JPLine plinemax = null;

	public int compareTo(JPLine o)
	{
		final int mask = 0x46; // code structure dependent info (as opposed to measurement dependent 0x20)
		
		int result = new Integer(offset).compareTo(o.offset);
		if (result == 0) {
			result = new Integer(flag0 & mask).compareTo(o.flag0 & mask);
		}
		return result;
	}
	
	public JPLine ()
	{
		flag0 = 0;
		flag1 = 0;
		flag2 = 0;
		offset = 0;
		count0 = 0;
		count1 = 0;
		startOfStmt = false;
		len = 0;
	}
	
	public void write (PrintStream outf)
	{
		outf.printf("P,%02x,%02x,%02x,%08x,%08x,%08x\n", flag0, flag1, flag2, offset, count0, count1);
	}

	public boolean covered() 
	{
		if (covermode.equalsIgnoreCase("C2")) {
			return (count0 > 0) && (((flag0 & 0x06) == 0x02) || (count1 > 0));  // C2 coverage version
		}
		else {
			return (flag0 & 0x20) != 0;  // C1 coverage version
		}
	}
	
	public static JPLine max ()
	{
		if (plinemax == null) {
			plinemax = new JPLine ();
			plinemax.offset = Integer.MAX_VALUE;
		}
		return plinemax;
	}

}
