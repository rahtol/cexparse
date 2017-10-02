package cextoolbox;
import java.io.PrintStream;


public class JStringLine extends JHeaderFooterLine {

	public String text0;
	public String text;
	
	@Override
	public	void write(PrintStream outf)
	{
		if (text0 != null) {
			outf.printf("!,%s,%s\n", text0, text);
		}
		else {
			outf.printf("!,%s\n", text);
		}
	}

	public JStringLine ()
	{
		this.text0 = null;
		this.text = "";
	}
	
	public JStringLine (String text0)
	{
		this.text0 = text0;
		this.text = "";
	}
}
