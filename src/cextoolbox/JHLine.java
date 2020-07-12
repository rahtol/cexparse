package cextoolbox;
import java.io.PrintStream;


public class JHLine extends JHeaderFooterLine {
	
	public int count0;
	public int count1;
	public String text;

	@Override
	public	void write(PrintStream outf)
	{
		outf.printf("H,%d,%d,%s\n", count0, count1, text);
	}

	public JHLine ()
	{
		count0 = 0;
		count1 = 0;
	}
}
