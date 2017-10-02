package cextoolbox;
import java.io.PrintStream;


public class JPLine implements Comparable<JPLine> {
	
	public int flag0;
	public int flag1;
	public int flag2;
	public int offset;
	public int count0;
	public int count1;

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
	}
	
	public void write (PrintStream outf)
	{
		outf.printf("P,%02x,%02x,%02x,%08x,%08x,%08x\n", flag0, flag1, flag2, offset, count0, count1);
	}

	public boolean covered() 
	{
//		return (flag0 & 0x20) != 0;  // C1 coverage version
		return (count0 > 0) && (((flag0 & 0x06) == 0x02) || (count1 > 0));  // C2 coverage version
	}

}
