import cextoolbox.CexFile;
import cextoolbox.JPLine;
import cextoolbox.jj.CexParser2;

public class CexMergeMain {
	
	// TODO: JPLine.covered must be adapted to COVERMODE 

	static String cexfname0;  // old cex-file-input
	static String cexfname1;  // new-cex-file-input 
	static String cexfname3;  // cex-file-output, i.e. new-cex-file-with-comments
	
	public static void main(String[] args)
	{
		System.out.printf("CexMergeMain v1.00, 2020-07-12\n");

		if (args.length >= 4) {
			JPLine.covermode = args[0];  // C1 or C2
			cexfname0 = args[1];  // old CEX-file used as reference respectively source of comments
			cexfname1 = args[2];  // current CEX-file to be completed with comments
			cexfname3 = args[3];  // the resulting CEX-file, i.e. main output of tool 
		}
		else {
			System.out.printf("usage: \"CexMerge <old-CEX-file> <new-CEX-file> <result-new-CEX-file-with-comments> [<diagnostic-output-file>]\"\n");
			System.exit(1);
		}
		
	    CexParser2 p_old = new CexParser2(cexfname0);
	    CexFile cexfile_old = p_old.parseCex();
	    CexParser2 p_new = new CexParser2(cexfname1);
	    CexFile cexfile_new = p_new.parseCex();
	    
	    if ((!p_old.parseOk) || (!p_new.parseOk)) {
	    	System.out.printf("Aborted due to parse errors.\n");
	    	System.exit(1);
	    }
	    
	    if (args.length >= 5) {
	    	// optionally generate a list of method with comparison status for each
	    	cexfile_new.compareBLines(cexfile_old, args[4]);
	    }
	    
	    cexfile_new.mergeComments(cexfile_old);
	    cexfile_new.writeCex(cexfname3);
	    cexfile_new.checkForGapsAndOverlaps();
	}

}
