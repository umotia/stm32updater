/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

public final class DfuState {
	public static final byte STATE_APP_IDLE = 0x00;
	public static final byte STATE_APP_DETACH = 0x01;
	public static final byte STATE_DFU_IDLE = 0x02;
	public static final byte STATE_DFU_DOWNLOAD_SYNC = 0x03;
	public static final byte STATE_DFU_DOWNLOAD_BUSY = 0x04;
	public static final byte STATE_DFU_DOWNLOAD_IDLE = 0x05;
	public static final byte STATE_DFU_MANIFEST_SYNC = 0x06;
	public static final byte STATE_DFU_MANIFEST = 0x07;
	public static final byte STATE_DFU_MANIFEST_WAIT_RESET = 0x08;
	public static final byte STATE_DFU_UPLOAD_IDLE = 0x09;
	public static final byte STATE_DFU_ERROR = 0x0a;
}
