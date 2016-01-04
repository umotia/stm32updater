/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.util.Vector;

public class DfuseMemoryLayout {
	public Vector<DfuseMemoryEntry> entries = null;
	
	public void parse(String sIface) {
		entries = new Vector<DfuseMemoryEntry>();
		String parts[] = sIface.split("/");
		for (int i=1;i<parts.length;i=i+2) {
			DfuseMemoryEntry entry = new DfuseMemoryEntry();
			entry.parseInterfaceString(parts[i], parts[i+1]);
			Stm32Updater.LOGGER.info(entry.toString());
			entries.add(entry);
		}
	}
	public DfuseMemoryEntry getEntry(long address) throws Exception {
		for (DfuseMemoryEntry entry : entries) {
			if (entry.isAddressInRange(address)) {
				return entry;
			}
		}
		throw new Exception(String.format("No valid memory map for address: 0x%08X",address));
	}
}
