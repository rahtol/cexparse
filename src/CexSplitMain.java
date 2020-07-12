import java.io.PrintStream;
import java.util.Iterator;

import cextoolbox.CexFile;
import cextoolbox.JBLine;
import cextoolbox.JHeaderFooterLine;
import cextoolbox.JPLine;
import cextoolbox.jj.CexParser2;

public class CexSplitMain {
	
	static String cexfname;
	static String cexoutdir = ".\\CexSplit.out";
	
	CexFile cexfile;

	public static void main(String[] args)
	{
		if (args.length == 1) {
			cexfname = args[0];   // input CEX-file to be split
		}
		else if (args.length == 2) {
			cexfname = args[0];   // input CEX-file to be split
			cexoutdir = args[1];  // output directory where there individual files go
		}
		else {
			System.out.printf("CexSplitMain: usage <input-CEX-file> [<output-directory>]\n");
			System.exit(3);
		}
		
	    CexParser2 p = new CexParser2(cexfname);
	    CexFile cexfile = p.parseCex();
	    
	    splitCex (cexfile, cexoutdir);
	}
	
	public static void splitCex (CexFile cexfile, String cexoutdir)
	{
		try { 
			// TODO: create or clear directory cexoutdir
			

			Iterator<JBLine> bi = cexfile.blist.iterator();
			while(bi.hasNext())
			{
				JBLine b = bi.next();
				PrintStream outf = new PrintStream (cexoutdir + "\\" + b.name.replace('*', '_').replace(',', '_'));
				b.write(outf);
				outf.close();
			}

		}
		catch (Exception e)
		{
			System.out.printf("Split NOK for \"%s\".\n", "???");
			System.out.println(e.getMessage());
		}
	}
	  

}
