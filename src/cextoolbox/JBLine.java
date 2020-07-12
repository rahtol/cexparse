package cextoolbox;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.TreeSet;


public class JBLine implements Comparable<JBLine> {

	public String name;
	public String signature;

	public int psize;  // number of p-lines according to counter in b-line
	public int csize;  // number of c-lines according to counter in b-line
	
	public NavigableSet<JPLine> plines;
	public NavigableSet<JCLine> clines;
	public LinkedList<JPLine> stmtIndex;

	static JBLine blineWithoutComments = new JBLine();
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
		stmtIndex = new LinkedList<JPLine>();
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
		return mergeComments(blineref, plines, 0);
	}
	
	boolean mergeComments (JBLine blineref, NavigableSet<JPLine> plines0, int offsetShift)
	{
		boolean ok = true; // whether all uncovered sections have been processed successfully
		int startOffs = 0;
		int endOffs = 0;
		boolean uncoveredSectionOpen = false; // whether the last line seen was uncovered
		JPLine pline = null;
		
		Iterator<JPLine> pi = plines0.iterator();
		while(pi.hasNext()) {
			pline = pi.next();
			if (uncoveredSectionOpen) {
				if (pline.covered()) {
					// complete uncovered section
					ok = processUncoveredSection (startOffs, endOffs, blineref.clines, offsetShift, pline) && ok;
					uncoveredSectionOpen = false;
				}
				else {
					// extend currently opened uncovered section
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
			ok = processUncoveredSection (startOffs, endOffs, blineref.clines, offsetShift, pline) && ok;
			uncoveredSectionOpen = false;
		}
		
		return ok;
	}
	
	private boolean isVcheck (JPLine plinesucc)
	{
		// unfortunately the successor of the INT5 statement is passed in
		JPLine pline = plines.lower(plinesucc);
		if (pline == null) return false;
		if (pline.len != 2) return false;
		JPLine pred = plines.lower(pline);
		if (pred == null) return false;
		return (pred.flag0 & 0x64) == 0x24; // branch instruction that is itself covered and not start of source code statement
	}

	private boolean processUncoveredSection(int startOffs, int endOffs,	NavigableSet<JCLine> refClines, int offsetShift, JPLine pline) 
	{
		lookup.offset0 = startOffs - offsetShift;
		JCLine comment = refClines.floor(lookup);
		JCLine comment2; 

		if (comment == null) {
			// no matching comment found. import cex did not show 100% coverage or new measurement was worse than that for import cex
			// System.out.printf("No match %s startOffs=0x%x, endOffs=0x%x\n", this.name, startOffs, endOffs);
			
			if ((startOffs == endOffs) && isVcheck(pline))
			{
				clines.add(new JCLine (startOffs, endOffs, "OTHER_INSTRUCTIONS(VCHECK)")); 
			}
			else {
				clines.add(new JCLine (startOffs, endOffs, "OTHER_INSTRUCTIONS(1)")); // optional: add a default comment
			}
			
			return false;
		}

		comment2 = new JCLine (comment);
		comment2.offset0 += offsetShift;
		comment2.offset1 += offsetShift;

		if (endOffs - offsetShift > comment.offset1) {
			// the comment does not cover the whole gap, i.e sequence of adjacent comments in import cex
			System.out.printf("EndOffs mismatch %s startOffs=0x%x 0x%x, endOffs=0x%x 0x%x\n", this.name, startOffs, comment.offset0, endOffs, comment.offset1);
			clines.add(new JCLine (startOffs, endOffs, comment.comment)); // optional: extend import comment range to cover whole gap
			return false;
		}
		if (endOffs - offsetShift != comment.offset1) {
			// the imported comment does cover more than required by the gap
//			System.out.printf("EndOffs overlap %s startOffs=0x%x 0x%x, endOffs=0x%x 0x%x, offsetShift=%d\n", this.name, startOffs, comment.offset0, endOffs, comment.offset1, offsetShift);
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
	
	public void buildStmtIndex ()
	{
		// also initializes the len in all but last pline
		
		int plinecnt = 0;
		Iterator<JPLine> pi = plines.iterator();
		JPLine pline = null;
		JPLine lastpline = null;
		while(pi.hasNext()) 
		{
			pline = pi.next();
			plinecnt++;

			// update len field of lastpline
			if (lastpline != null) {
				lastpline.len = pline.offset - lastpline.offset;
			}
			lastpline = pline;
			
			// check if pline is start of new source statement
			if ((plinecnt==1) || ((pline.flag0 & 0x40) == 0x40)) {
				pline.startOfStmt = true;
				stmtIndex.add(pline);
			}
		}	
	}

	public boolean calcMatchingSourceStmts (JBLine blineref)
	{
		// for a first shot only the number of statements must coincide
		return this.stmtIndex.size() == blineref.stmtIndex.size();
	};

	boolean stmtsAreCompatible (JBLine blineref, JPLine plinestmt, JPLine plinestmtref)
	{
		int pcnt = 0;
		Iterator<JPLine> stmt = plines.tailSet(plinestmt).iterator(); 
		Iterator<JPLine> stmtref = blineref.plines.tailSet(plinestmtref).iterator(); 
		if (!(stmt.hasNext() && stmtref.hasNext())) return false; // exclude the empty statement
		
		while (true) {
			if (stmt.hasNext() != stmtref.hasNext()) return false; // only one set exhausted, delivers incompatible statement since number of statements in method is equal
			if (!stmt.hasNext()) return true; // both sets are exhausted in sync, delivers compatible statement
			// here, both sets have another pline
			JPLine pstmt = stmt.next();
			JPLine pstmtref = stmtref.next();
			pcnt++;
			if (pstmt.startOfStmt != pstmtref.startOfStmt) return false;
			if (pstmt.startOfStmt && pcnt > 1) return true;
			if (pstmt.len != pstmtref.len) return false;
		}
	};
	
	public void mergeCommentsByStmt (JBLine blineref)
	{
		// merge statement by statement
		// this and blineref have same number of source statements
		ListIterator<JPLine> si = stmtIndex.listIterator();
		Iterator<JPLine> siref = blineref.stmtIndex.iterator();
		@SuppressWarnings("unused")
		int ok = 0;
		@SuppressWarnings("unused")
		int nok = 0;
		while (si.hasNext()) {
			JPLine plinestmt = si.next();
			JPLine plinestmtref = siref.next();
			JPLine plinestmtnext = null;
			if (si.hasNext()) {
				plinestmtnext =  si.next();
				si.previous();
			}
			else {
				plinestmtnext = JPLine.max();
			}
			if (stmtsAreCompatible (blineref, plinestmt, plinestmtref))
			{
				if (mergeComments(blineref, plines.subSet(plinestmt, true, plinestmtnext, false), plinestmt.offset - plinestmtref.offset)) {
					ok++;
				}
				else {
					nok++;
				}
			}
			else {
				mergeComments(blineWithoutComments, plines.subSet(plinestmt, true, plinestmtnext, false), 0);
				nok++;
			}
		}
		// System.out.printf("mergeCommentsByStmt(%s): ok=%d, nok=%d\n", this.name, ok, nok);
	};

}
