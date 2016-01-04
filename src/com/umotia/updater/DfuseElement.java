/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.io.DataInputStream;

public class DfuseElement {
	public long address = 0;
	public long elementSize = 0;
	public byte data[] = null;
	public void read(DataInputStream in) throws Exception {
		address = DfuUtils.readIntFromBytes(in);
		elementSize = DfuUtils.readIntFromBytes(in);
		
		data = new byte[(int) elementSize];
		in.read(data);
		Stm32Updater.LOGGER.info(String.format("Read Image Element: 0x%08x, %d", address, elementSize));
	}
}
