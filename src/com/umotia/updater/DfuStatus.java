/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

public final class DfuStatus {
	public static final byte DFU_STATUS_OK                   = 0x00;
	public static final byte DFU_STATUS_ERROR_TARGET         = 0x01;
	public static final byte DFU_STATUS_ERROR_FILE           = 0x02;
	public static final byte DFU_STATUS_ERROR_WRITE          = 0x03;
	public static final byte DFU_STATUS_ERROR_ERASE          = 0x04;
	public static final byte DFU_STATUS_ERROR_CHECK_ERASED   = 0x05;
	public static final byte DFU_STATUS_ERROR_PROG           = 0x06;
	public static final byte DFU_STATUS_ERROR_VERIFY         = 0x07;
	public static final byte DFU_STATUS_ERROR_ADDRESS        = 0x08;
	public static final byte DFU_STATUS_ERROR_NOTDONE        = 0x09;
	public static final byte DFU_STATUS_ERROR_FIRMWARE       = 0x0a;
	public static final byte DFU_STATUS_ERROR_VENDOR         = 0x0b;
	public static final byte DFU_STATUS_ERROR_USBR           = 0x0c;
	public static final byte DFU_STATUS_ERROR_POR            = 0x0d;
	public static final byte DFU_STATUS_ERROR_UNKNOWN        = 0x0e;
	public static final byte DFU_STATUS_ERROR_STALLEDPKT     = 0x0f;
}
