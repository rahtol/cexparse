package cexsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cexsearch.jj.CexParser;


public class CexSearch {

	public String signature;  // of the method
	public int offset; // of instruction within method
	public int noCexFiles;
	public int noCexMethodFound;
	
	public CexSearch (String signature, int offset)
	{
		this.signature = signature;
		this.offset = offset;
		this.noCexFiles = 0;
		this.noCexMethodFound = 0;;
	}

	public void parseDir (final File folder) throws IOException
	{
		parseDir(0, folder);
	}

	void parseDir (int level, final File folder) throws IOException
	{
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            parseDir (level+1, fileEntry);
	        } else if (fileEntry.isFile()) {
	        	processFile (level+1, fileEntry);
	        } else {
	        	System.err.println(fileEntry.getAbsolutePath());
	        }
	    }
	}

	void processFile (int level, final File file) throws IOException
	{
		if (file.getName().endsWith(".cex"))
		{
			noCexFiles++;

//			processCex (file);

			CexParser p = new CexParser(file.getAbsolutePath(), signature, offset);
		    p.parseCex();
		    if ((p.bFound) && (p.count0+p.count1 >0)) {
		    System.out.printf(
		        "Result for signature=%s, offset=%04x, cexfname=\u005c"%s\u005c": bfound=%d, lineno=%d, count0=%d, count1=%d\u005cn",
		        p.signature, p.offset, p.cexfname, (p.bFound ? 1 : 0), p.lineno, p.count0, p.count1);
		    }
			if (p.bFound) noCexMethodFound++; 
			
//			System.out.println(file.getAbsolutePath());
			/*
			Scanner sc = new Scanner(file);
			String m00 = sc.findWithinHorizon("B,.*updateListOfLocalTrains__Q2_3SEP15CSeparationMainFv\\(void\\)", 0);
			String m01 = sc.findWithinHorizon("P,..,..,..,0000030c,........,........", 0);
			System.out.println(m01 + "  " + file.getAbsolutePath());
			sc.close(); 
			*/
		}
	}


	final static Pattern B_line = Pattern.compile ("B,([0-9a-fA-F]{32}),([0-9a-fA-F]+),([0-9a-fA-F]+),(.*)");
	final static Pattern P_line =  Pattern.compile ("P,([0-9a-fA-F]{2}),([0-9a-fA-F]{2}),([0-9a-fA-F]{2}),([0-9a-fA-F]{8}),([0-9a-fA-F]{8}),([0-9a-fA-F]{8})");
	
	void processCex (final File file) throws IOException
	{
		BufferedReader f = new BufferedReader (new FileReader(file));
		String s;
		boolean inside = false;
		@SuppressWarnings("unused")
		int lineno = 0;
		int noCexMethodFoundLocal = 0;
		
		while ((s = f.readLine()) != null)
		{
			lineno++;
			Matcher b_line = B_line.matcher(s);
			Matcher p_line = P_line.matcher(s);
			
			if (b_line.matches()) {
				String b_signature = b_line.group(1);
				boolean b = b_signature.equals(signature);
				if (b) noCexMethodFoundLocal++;
				inside = b;
			}
			
			if (p_line.matches()) {
				String p_offs = p_line.group(4);
				int offs = Integer.parseInt(p_offs, 16);
				
				if (inside && offs == offset) {
					String p_count = p_line.group(5);
					int count = Integer.parseInt(p_count, 16);
					if (count > 0)
					{
						System.out.println(p_count + "  " + file.getAbsolutePath());
					}
				}
			}
			
		};
		
		f.close ();

		if (noCexMethodFoundLocal > 1)
		{
			System.err.println ("Multiple Method in: " + file.getAbsolutePath());
		}
		
		if (noCexMethodFoundLocal < 1)
		{
			System.err.println ("Method not found in: " + file.getAbsolutePath());
		}

		if (noCexMethodFoundLocal > 0) noCexMethodFound++; 
	}
}
