/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

public final class DfuCommand {
	public static final byte DFU_DETACH    = 0x00;
	public static final byte DFU_DNLOAD    = 0x01;
	public static final byte DFU_UPLOAD    = 0x02;
	public static final byte DFU_GETSTATUS = 0x03;
	public static final byte DFU_CLRSTATUS = 0x04;
	public static final byte DFU_GETSTATE  = 0x05;
	public static final byte DFU_ABORT     = 0x06;
	
	public static final byte DFU_DNLOAD_WRITE          = 0x10;
	public static final byte DFU_DNLOAD_SET_ADDRESS    = 0x21;
	public static final byte DFU_DNLOAD_ERASE          = 0x41;
	public static final byte DFU_DNLOAD_MASS_ERASE     = 0x42;
	public static final byte DFU_DNLOAD_READ_UNPROTECT = (byte) 0x92;
}
