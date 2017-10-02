import cextoolbox.CexFile;
import cextoolbox.jj.CexParser2;

public class CexMergeMain {
	
	// TODO: JPLine.covered must be adapted to COVERMODE 

//	static String cexfname0 = "D:\\work\\MTC_LIF\\COV\\code_coverage_C2_R3_0_7.cex";
	static String cexfname0 = "D:\\work\\MTC_TRA_2017_06\\TRA_C2_COVERAGE_3_0_8\\170612_1030_C1_C2_all_tests_with_comments.cex";
//	static String cexfname1 = "D:\\work\\MTC_LIF\\COV\\all_testsC2.cex";
	static String cexfname1 = "D:\\work\\MTC_TRA_2017_06\\TRA_C2_COVERAGE_3_0_8\\170612_1030_C1_C2_all_tests_with_comments.cex";
	static String cexfname2 = "D:\\work\\MTC_TRA_2017_06\\all_testsC2.ls0";
//	static String cexfname3 = "D:\\work\\MTC_TRA_2017_06\\all_testsC2.c0x";
	static String cexfname3 = "D:\\work\\MTC_TRA_2017_06\\TRA_C2_COVERAGE_3_0_8\\170612_1030_C1_C2_all_tests_with_comments.c0x";
	
	public static void main(String[] args)
	{
	    CexParser2 p_old = new CexParser2(cexfname0);
	    CexFile cexfile_old = p_old.parseCex();
	    CexParser2 p_new = new CexParser2(cexfname1);
	    CexFile cexfile_new = p_new.parseCex();
	    cexfile_new.compareBLines(cexfile_old, cexfname2);
//	    cexfile_old.flag0valuelist();
//	    cexfile_new.flag0valuelist();
//	    cexfile_new.mergeComments(cexfile_old);
//	    cexfile_new.mergeComments(null);
//	    cexfile_new.writeCex(cexfname3);
	    cexfile_new.checkForGapsAndOverlaps();
	}

}
