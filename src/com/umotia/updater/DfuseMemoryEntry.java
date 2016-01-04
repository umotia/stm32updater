/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DfuseMemoryEntry {
	public long address = 0;
	public int numSectors = 0;
	public int sectorSize = 0;
	public int pageSize = 0;
	public int sectorSizeMult = 1;
	public boolean readable = false;
	public boolean writeable = false;
	public boolean eraseable = false;
	
	public boolean isAddressInRange(long a) {
		if (a >= address && address < (a+(numSectors*pageSize))) {
			return true;
		}
		return false;
	}
	public void parseInterfaceString(String sAddress, String memory) {
		readable = false;
		writeable = false;
		eraseable = false;
		
		address = Long.decode(sAddress);
		Pattern r = Pattern.compile("(\\d+)\\*(\\d+)([KMB])([abcdefg])");
		Matcher m = r.matcher(memory);
		if (m.find( )) {
			numSectors = Integer.parseInt(m.group(1));
			sectorSize = Integer.parseInt(m.group(2));
			String units = m.group(3);
			if (units.equals("K")) { sectorSizeMult = 1024; }
			else if(units.equals("M")) { sectorSizeMult = 1048576; }
			
			String type = m.group(4);
			if (type.equals("a")) { readable = true; }
			else if (type.equals("b")) { eraseable = true; }
			else if (type.equals("c")) { readable = true; eraseable = true; }
			else if (type.equals("d")) { writeable = true; }
			else if (type.equals("e")) { readable = true; writeable = true; }
			else if (type.equals("f")) { writeable = true; eraseable = true; }
			else if (type.equals("g")) { readable = true; writeable = true; eraseable = true; }
			pageSize = sectorSize*sectorSizeMult;
		}
	}
	public String toString() {
		String mode = "";
		if (readable) { mode = mode+"R"; }
		if (writeable) { mode = mode+"W"; }
		if (eraseable) { mode = mode+"E"; }
		String r = String.format("Memory Layout: Address = 0x%08X; # of sectors = %d; page size = %d; Mode = %s",
				address,numSectors,pageSize,mode);
		return r;
	}
}
