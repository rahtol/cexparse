package cextoolbox;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;


public class CexFile {
	
	  public String cexfname;
	  public NavigableSet<JBLine> btable;  // B-sections sorted by name
	  public List<JBLine> blist;  // B-sections in order of occurrence
	  public List<JHeaderFooterLine> header;  // H-lines in order of occurrence
	  public List<JHeaderFooterLine> footer;  // footer-lines in order of occurrence
	  
	  static JBLine blineWithoutComments = new JBLine();

	  public CexFile (String cexfname, List<JHeaderFooterLine> header, List<JBLine> blist, NavigableSet<JBLine> btable, List<JHeaderFooterLine> footer)
	  {
		  this.cexfname = cexfname;
		  this.header = header;
		  this.blist = blist;
		  this.btable = btable;
		  this.footer = footer;
	  }

	  public void writeCex (String outfname)
	  {
	      try { 
			PrintStream outf = new PrintStream (outfname);
		
			Iterator<JHeaderFooterLine> iheader = header.iterator();
			while(iheader.hasNext())
			{
				iheader.next().write(outf);
			}
			
			Iterator<JBLine> bi = blist.iterator();
			while(bi.hasNext())
			{
				bi.next().write(outf);
			}

			Iterator<JHeaderFooterLine> ifooter = footer.iterator();
			while(ifooter.hasNext())
			{
				ifooter.next().write(outf);
			}
			
			outf.close();

		  }
	      catch (Exception e)
	      {
	        System.out.printf("Write NOK for \"%s\".\n", outfname);
	        System.out.println(e.getMessage());
	      }
	  }
	  
	  public void compareBLines (CexFile refCex, String outfname)
	  {
		  int methodCnt = 0;
		  int existingMethodCnt = 0;
		  int matchingLinesCnt = 0;
		  int matchingSignaturesCnt = 0;
		  
	      try { 
			PrintStream outf = new PrintStream (outfname);
			
			Iterator<JBLine> bi = btable.iterator();
			while (bi.hasNext())
			{
				JBLine bline = bi.next();
				methodCnt++;
				JBLine blineref = refCex.btable.ceiling(bline);
				Boolean existingMethod = (blineref==null ? false : bline.name.equals(blineref.name));
				Boolean matchingLines = false;
				Boolean matchingSignatures = false;
				
				if (existingMethod)	
				{
					matchingLines = bline.plines.equals(blineref.plines);
					matchingSignatures = bline.signature.equals(blineref.signature);
					existingMethodCnt++;
					if (matchingLines)  matchingLinesCnt++;
					if (matchingSignatures)  matchingSignaturesCnt++;
				}
				
				outf.printf("%s $0$%d $1$%d $2$%d\n", bline.name, (existingMethod?1:0), (matchingLines?1:0), (matchingSignatures?1:0));
			}
			
			outf.close();
			
			System.out.printf("%s methodCnt=%d existingMethodCnt=%d matchingLinesCnt=%d matchingSignatures=%d\n", this.cexfname, methodCnt, existingMethodCnt, matchingLinesCnt, matchingSignaturesCnt);
			
		  }
	      catch (Exception e)
	      {
	        System.out.printf("compareBLines NOK for \"%s\".\n", outfname);
	        System.out.println(e.getMessage());
	      }
	  }
	  
	  public void flag0valuelist ()
	  {
		int flag0cnt [] = new int[256];
		
		Iterator<JBLine> bi = btable.iterator();
		while (bi.hasNext())
		{
			JBLine bline = bi.next();
			Iterator<JPLine> pi = bline.plines.iterator();
			while (pi.hasNext()) {
				flag0cnt [pi.next().flag0]++;
			}
		}

		System.out.printf("%s", this.cexfname);
		int total = 0;
		for (int i=0; i<256;i++) {
			if (flag0cnt[i]!=0) {
				System.out.printf(" %02x(%d)", i, flag0cnt[i]);
			};
			total += flag0cnt[i];
		}
		System.out.printf(" Total(%d)\n", total);
	  }
	  
	  public void purgeComments ()
	  {
			Iterator<JBLine> bi = btable.iterator();
			while (bi.hasNext())
			{
				JBLine bline = bi.next();
				bline.clines.clear();
			}
	  }

	  public void mergeComments (CexFile refCex)
	  {
		  int methodCnt = 0;
		  int existingMethodCnt = 0;
		  int matchingLinesCnt = 0;
		  int mergeCompleteCnt = 0;

		  try 
		  {
			  this.purgeComments();

			  Iterator<JBLine> bi = btable.iterator();
			  while (bi.hasNext())
			  {
				  JBLine bline = bi.next();
				  methodCnt++;
				  JBLine blineref = refCex.btable.ceiling(bline);
				  Boolean existingMethod = (blineref==null ? false : bline.name.equals(blineref.name));
				  Boolean matchingLines = false;
				  Boolean mergeComplete = false;

				  if (existingMethod)	
				  {
					  matchingLines = bline.plines.equals(blineref.plines);
					  existingMethodCnt++;
					  if (matchingLines) 
					  {
						  matchingLinesCnt++;
						  mergeComplete = bline.mergeComments(blineref);
						  if (mergeComplete) mergeCompleteCnt++;
					  }
				  }
				  
				  
				  if (!matchingLines) {
					  // create default comments in order to achieve 100% in created CEX
					  // do so by adding dummy comments that can be post-precessed manually
					  bline.mergeComments(blineWithoutComments);
				  }
			  }

			  System.out.printf("mergeComments: %s %s methodCnt=%d existingMethodCnt=%d matchingLinesCnt=%d mergeCompleteCnt=%d\n", this.cexfname, refCex.cexfname, methodCnt, existingMethodCnt, matchingLinesCnt, mergeCompleteCnt);

		  }
		  catch (Exception e)
		  {
			  System.out.printf("mergeComments NOK for \"%s\".\n", this.cexfname);
			  System.out.println(e.getMessage());
		  }
	  }
	  
	  public void checkForGapsAndOverlaps ()
	  {
		  Iterator<JBLine> bi = btable.iterator();
		  while (bi.hasNext())
		  {
			  JBLine bline = bi.next();
			  bline.checkForGapsAndOverlaps();
		  }
	  }
	  
}
