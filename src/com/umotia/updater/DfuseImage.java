/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.io.DataInputStream;
import java.util.Vector;

public class DfuseImage {
	@SuppressWarnings("unused")
	private String signature = "";
	private byte alt = 0x00;
	private boolean isNamed = false;
	@SuppressWarnings("unused")
	private long targetSize = 0;
	private long numElements = 0;
	String name = "";
	Vector<DfuseElement> elements = null;
	
	public byte getAlt() {
		return alt;
	}
	public Vector<DfuseElement> getElements() {
		return elements;
	}
	private void readTargetPrefix(DataInputStream in) throws Exception {
		signature = DfuUtils.readStringFromBytes(in, 6);
		alt = in.readByte();
		byte n[] = new byte[4];
		in.read(n);
		if (n[0] == 0x00) { isNamed = false; }
		else { isNamed = true; }
		
		byte str[] = new byte[255];
		in.read(str);
		
		int len = 0;
		while (len<255 && str[len]!=0x00) {
			len++;
		}
		byte str_cat[] = new byte[len];
		for (int i=0;i<len;i++) {
			str_cat[i] = str[i];
		}
		if (isNamed) {
			name = new String(str_cat,"UTF-8");
		}
		targetSize = DfuUtils.readIntFromBytes(in);
		numElements = DfuUtils.readIntFromBytes(in);		
		Stm32Updater.LOGGER.info("Target read: "+name+"; Alt: "+alt);
		
		elements = new Vector<DfuseElement>();
		for (int i=0;i<numElements;i++) {
			DfuseElement element = new DfuseElement();
			element.read(in);
			elements.add(element);
		}
	}
	public void read(DataInputStream in) throws Exception {
		readTargetPrefix(in);
	}
}
