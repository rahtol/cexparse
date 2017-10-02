import java.io.File;
import java.io.IOException;

import cexsearch.CexSearch;

public class testcexsearch
{
	public static void main(String[] args)
	{
		CexSearch cex1 = new CexSearch ("2583aa543a521b4cb0ee502e50671655", 0x30c);
		
		try {
			cex1.parseDir (new File ("D:\\tmp\\TGMT_R3_SwCompTstRep_WCU_ATP_SEP_Embedded01\\rttester"));

			System.out.println("noCexFiles=" + cex1.noCexFiles);
			System.out.println("noCexMethodFound=" + cex1.noCexMethodFound);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
