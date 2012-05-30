package com.bbpos.cswiper.encrypt;

/**
 * @author Derek
 * 
 */

public class Rambler {

	// format ID: 31 - AES, DUKPT, not support yet
	//            32 - Plain text, not support yet
	//            33 - TDES, DUKPT, not support yet
	//            34 - TDES, DUKPT
	//            35 - TDES, DUKPT, not support yet
	//            36 - TDES, Fixed Key 
	//            38 - TDES, DUKPT
	public static String[] decryptTrack(int formatID, String encTrack, String KSN, String BDK)
	{
		int i;
		String[] Track = new String[3];
		byte[] bEncTrack, bTrack, bKey;
		String sTrack;
		int t1S, t1E, t2S, t2E, t3S, t3E;
		
		Track[0] = "";
		Track[1] = "";
		Track[2] = "";
		
		// get key
		bKey = null;
		if ((formatID == 0x31) ||
			(formatID == 0x33) ||
			(formatID == 0x34) ||
			(formatID == 0x35) ||
			(formatID == 0x38))
		{
			bKey = DES.String2Hex(DUKPTServer.GetDataKey(KSN, BDK));
		}
		else if (formatID == 0x36)
		{
			bKey = DES.String2Hex(DUKPTServer.GetFixedKey(KSN, BDK));
		}
		
		// decrypt track data
		bEncTrack = DES.String2Hex(encTrack);
		if (formatID == 0x31)
			bTrack = AES.decrypt(bEncTrack, bKey);
		else if (formatID == 0x32)
		{
			bTrack = new byte[bEncTrack.length];
			for (i=0; i<bEncTrack.length; i++)
				bTrack[i] = bEncTrack[i];
		}
		else
			bTrack = TripleDES.decrypt_CBC(bEncTrack, bKey);
		
		// convert to String
		sTrack = "";
		for (i=0; i<bTrack.length; i++)
		{
			if ((bTrack[i] >= 0x20) && (bTrack[i] < 0x7B)) 
				sTrack += (char)bTrack[i];
		}
		
		t1S = -1;
		t1E = -1;
		t2S = -1;
		t2E = -1;
		t3S = -1;
		t3E = -1;
		if ((formatID == 0x31) ||
			(formatID == 0x32) ||
			(formatID == 0x36) ||
			(formatID == 0x38))
		{
			// search for track 1 and track 2
			t1S = sTrack.indexOf('%');
			if (t1S >= 0)
				t1E = sTrack.indexOf('?', t1S+1);
			if (t1E >= 0)
				t2S = sTrack.indexOf(';', t1E+1);
			else
				t2S = sTrack.indexOf(';');
			if (t2S >= 0)
				t2E = sTrack.indexOf('?', t2S+1);
			
			if ((t1S >= 0) && (t1E > t1S))
				Track[0] = sTrack.substring(t1S, t1E+1);
			if ((t2S >= 0) && (t2E > t2S))
				Track[1] = sTrack.substring(t2S, t2E+1);
		}
		else if (formatID == 0x33)
		{
			// search for track 1
			t1S = sTrack.indexOf('%');
			if (t1S >= 0)
				t1E = sTrack.indexOf('?', t1S+1);
			if ((t1S >= 0) && (t1E > t1S))
				Track[0] = sTrack.substring(t1S, t1E+1);
		}
		else if (formatID == 0x34)
		{
			// search for track 2 and track 3
			t2S = sTrack.indexOf(';');
			if (t2S >= 0)
				t2E = sTrack.indexOf('?', t2S+1);
			if (t2E >= 0)
				t3S = sTrack.indexOf(';', t2E+1);
			else
				t3S = sTrack.indexOf(';');
			if (t3S >= 0)
				t3E = sTrack.indexOf('?', t3S+1);
			
			if ((t2S >= 0) && (t2E > t2S))
				Track[1] = sTrack.substring(t2S, t3E+1);
			if ((t3S >= 0) && (t3E > t3S))
				Track[2] = sTrack.substring(t3S, t3E+1);
		}
		else if (formatID == 0x35)
		{
			// search for track 2
			t2S = sTrack.indexOf(';');
			if (t2S >= 0)
				t2E = sTrack.indexOf('?', t2S+1);
			if ((t2S >= 0) && (t2E > t2S))
				Track[1] = sTrack.substring(t2S, t3E+1);
		}
		
		return Track;
	}
	
	// format ID: 31 - AES, DUKPT, not support yet
	//            32 - Plain text, not support yet
	//            33 - TDES, DUKPT, not support yet
	//            34 - TDES, DUKPT
	//            35 - TDES, DUKPT, not support yet
	//            36 - TDES, Fixed Key 
	//            38 - TDES, DUKPT
	public static String[] decryptTrack(int formatID, String encTrack, int T1Len, int T2Len, int T3Len, String KSN, String BDK)
	{
		int i;
		String[] Track = new String[3];
		byte[] bEncTrack, bTrack, bKey;
		String sTrack;
		int start, end;
		
		Track[0] = "";
		Track[1] = "";
		Track[2] = "";
		
		// get key
		bKey = null;
		if ((formatID == 0x31) ||
			(formatID == 0x33) ||
			(formatID == 0x34) ||
			(formatID == 0x35) ||
			(formatID == 0x38))
		{
			bKey = DES.String2Hex(DUKPTServer.GetDataKey(KSN, BDK));
		}
		else if (formatID == 0x36)
		{
			bKey = DES.String2Hex(DUKPTServer.GetFixedKey(KSN, BDK));
		}
		
		// decrypt track data
		bEncTrack = DES.String2Hex(encTrack);
		if (formatID == 0x31)
			bTrack = AES.decrypt(bEncTrack, bKey);
		else if (formatID == 0x32)
		{
			bTrack = new byte[bEncTrack.length];
			for (i=0; i<bEncTrack.length; i++)
				bTrack[i] = bEncTrack[i];
		}
		else
			bTrack = TripleDES.decrypt_CBC(bEncTrack, bKey);
		
		// convert to String
		sTrack = "";
		for (i=0; i<bTrack.length; i++)
		{
			if ((bTrack[i] >= 0x20) && (bTrack[i] < 0x7B)) 
				sTrack += (char)bTrack[i];
		}

		// extract Track
		start = 0;
		end = 0;
		if (T1Len > 0)
		{
			end = T1Len;
			Track[0] = sTrack.substring(start, end);
			start = end;
		}
		if (T2Len > 0)
		{
			end = start + T2Len;
			Track[1] = sTrack.substring(start, end);
			start = end;
		}
		if (T3Len > 0)
		{
			end = start + T3Len;
			Track[2] = sTrack.substring(start, end);
		}
		
		return Track;
	}
	
}

