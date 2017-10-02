package cextoolbox;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;


public class JBLine implements Comparable<JBLine> {

	public String name;
	public String signature;

	public int psize;  // number of p-lines according to counter in b-line
	public int csize;  // number of c-lines according to counter in b-line
	
	public NavigableSet<JPLine> plines;
	public NavigableSet<JCLine> clines;

	static JCLine lookup = new JCLine();
	
	public int compareTo(JBLine o)
	{
		return name.compareTo(o.name);
	}
	
	public JBLine ()
	{
		name = "";
		signature = "";
		plines = new TreeSet<JPLine>();
		clines = new TreeSet<JCLine>();
	}

	public void write (PrintStream outf)
	{
		outf.printf("B,%s,%d,%d,%s\n", signature, plines.size(), clines.size(), name);
		
		Iterator<JPLine> pi = plines.iterator();
		while(pi.hasNext()){
			pi.next().write(outf);
		}
		
		Iterator<JCLine> ci = clines.iterator();
		while(ci.hasNext()){
			ci.next().write(outf);
		}
	}
	
	boolean mergeComments (JBLine blineref)
	{
		boolean ok = true; // whether all uncovered sections have been processed succesfully
		int startOffs = 0;
		int endOffs = 0;
		boolean uncoveredSectionOpen = false; // whether the last line seen was uncovered
		
		Iterator<JPLine> pi = plines.iterator();
		while(pi.hasNext()) {
			JPLine pline = pi.next();
			if (uncoveredSectionOpen) {
				if (pline.covered()) {
					// complete uncovered section
					ok = processUncoveredSection (startOffs, endOffs, blineref.clines) && ok;
					uncoveredSectionOpen = false;
				}
				else {
					// extend currently opened uncovered setion
					endOffs = pline.offset;
				}
			}
			else {
				if (pline.covered()) {
					// nothing to do 
				}
				else {
					// start new uncovered section
					startOffs = pline.offset;
					endOffs = pline.offset;
					uncoveredSectionOpen = true;
				}
			}
		}
		
		if (uncoveredSectionOpen) {
			ok = processUncoveredSection (startOffs, endOffs, blineref.clines) && ok;
			uncoveredSectionOpen = false;
		}
		
		return ok;
	}

	private boolean processUncoveredSection(int startOffs, int endOffs,	NavigableSet<JCLine> refClines) 
	{
		lookup.offset0 = startOffs;
		JCLine comment = refClines.floor(lookup);
		JCLine comment2; 

		if (comment == null) {
			// no matching comment found. import cex did not show 100% coverage or new measurement was worse than that for import cex
			System.out.printf("No match %s startOffs=0x%x, endOffs=0x%x\n", this.name, startOffs, endOffs);
			clines.add(new JCLine (startOffs, endOffs, "OTHER_INSTRUCTIONS(1)")); // optional: add a default comment
			return false;
		}

		comment2 = new JCLine (comment);

		if (endOffs > comment.offset1) {
			// the comment does not cover the whole gap, i.e sequence of adjacent comments in import cex
			System.out.printf("EndOffs mismatch %s startOffs=0x%x 0x%x, endOffs=0x%x 0x%x\n", this.name, startOffs, comment.offset0, endOffs, comment.offset1);
			clines.add(new JCLine (startOffs, endOffs, comment.comment)); // optional: extend import comment range to cover whole gap
			return false;
		}
		if (endOffs != comment.offset1) {
			// the imported comment does cover more than required by the gap
			System.out.printf("EndOffs overlap %s startOffs=0x%x 0x%x, endOffs=0x%x 0x%x\n", this.name, startOffs, comment.offset0, endOffs, comment.offset1);
			comment2.offset1 = endOffs;
		}

		// if the comment overlaps with an covered instruction XDB386 does not accept the whole comment
		// thus sometimes (-VCHECK) the
		comment2.offset0 = startOffs;
		clines.add(comment2);
		return true;
	}

	void checkForGapsAndOverlaps ()
	{
		Iterator<JPLine> pi = plines.iterator();
		while(pi.hasNext()) 
		{
			JPLine pline = pi.next();
			
			boolean covered =  pline.covered();
			boolean commented = commentAt(pline.offset);
			
			boolean ovl = covered && commented;
			boolean gap = !covered && !commented;
			
			if (ovl) {
				// overlap between coverage and comment
//				System.out.printf("Instruction/Comment overlap: %s 0x%x\n", this.name, pline.offset);
			}
			if (gap) {
				// instruction neither covered nor commented
				System.out.printf("Instruction not covered: %s 0x%x\n", this.name, pline.offset);
			}
		}
	}
	
	boolean commentAt (int offset)
	{
		lookup.offset0 = offset;
		JCLine comment = clines.floor(lookup);
		return (comment != null) && (offset >= comment.offset0) && (offset <= comment.offset1);
	}

}
