package cextoolbox;
import java.io.PrintStream;


public class JCLine implements Comparable<JCLine> {
	
	public int offset0;
	public int offset1;
	public String comment;

	public int compareTo(JCLine o)
	{
		return new Integer(offset0).compareTo(o.offset0);
	}
		
	public JCLine ()
	{
			offset0 = 0;
			offset1 = 0;
			comment = "";
	}
		
	public JCLine(JCLine cline)
	{
		offset0 = cline.offset0;
		offset1 = cline.offset1;
		comment = cline.comment;
	}

	public JCLine(int startOffs, int endOffs, String text) {
		offset0 = startOffs;
		offset1 = endOffs;
		comment = text;
	}

	public void write (PrintStream outf)
	{
		outf.printf("C,%08x,%08x,%s\n", offset0, offset1, comment);
	}

}
