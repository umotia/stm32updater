/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

public class DfuseFile {
	@SuppressWarnings("unused")
	private String signature = "";
	@SuppressWarnings("unused")
	private byte version = 0x00;
	@SuppressWarnings("unused")
	private long fileLength = 0;
	private byte targets = 0;
	private String device = "";
	private short product = 0x0000;
	private short vendor = 0x0000;
	private short dfu_num = 0;
	@SuppressWarnings("unused")
	private String suffixSignature = "";
	@SuppressWarnings("unused")
	private byte suffixLength = 0;
	private long crc = 0;
	private Vector<DfuseImage> images = null;
	
	public short getVendor() { return vendor; }
	public short getProduct() { return product; }
	public Vector<DfuseImage> getImages() { return images; }
	
	private void readPrefix(DataInputStream in) throws Exception {
		signature = DfuUtils.readStringFromBytes(in, 5);
		version = in.readByte();
		fileLength = DfuUtils.readIntFromBytes(in);
		targets = in.readByte();
	}
	private void readSuffix(DataInputStream in) throws Exception {
		device =  DfuUtils.readStringFromBytes(in,2);
		product =  DfuUtils.readShortFromBytes(in);
		vendor =  DfuUtils.readShortFromBytes(in);
		dfu_num =  DfuUtils.readShortFromBytes(in);
		suffixSignature =  DfuUtils.readStringFromBytes(in,3);
		suffixLength = in.readByte();
		crc = DfuUtils.readUnsignedIntFromBytes(in);
		
		if (dfu_num != 282) {
			throw new Exception("Invalid file version: "+dfu_num);
		}
		String p = String.format("Version: %s; Vendor: 0x%04X; Product: 0x%04X", device,vendor,product);
		Stm32Updater.LOGGER.info(p);
	}
	
	public long getCrc(String filename) throws Exception {
		File file = new File(filename);
		DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(filename)));

		long crc_file = 0xffffffffL;
		for (long i = 0; i<file.length()-4; i++) {
			crc_file = DfuUtils.crc32_byte(crc_file, in.readUnsignedByte());
		}
		in.close();
		return crc_file;
	}
	public void readFile(String filename) throws Exception {
		DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(filename)));
		
		readPrefix(in);
		images = new Vector<DfuseImage>();
		for (int i=0;i<targets;i++) {
			DfuseImage image = new DfuseImage();
			image.read(in);
			images.add(image);
		}
		readSuffix(in);
		in.close();
		
		if (getCrc(filename) != crc) {
			throw new Exception("File is corrupt: "+filename);
		}
	}
}
