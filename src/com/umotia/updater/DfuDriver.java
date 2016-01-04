/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;

public class DfuDriver {
	public byte status = 0x00;
	public int pollTimeout = 0;
	public byte state = 0x00;
	public byte str = 0x00;
	
	UsbDevice device = null;
		
	public DfuDriver(UsbDevice device) {
		this.device = device;
	}
	public byte getState() {
		return state;
	}
	public void parseStatus(byte[] buffer) {
		status = buffer[0];
		pollTimeout = ((0xff & buffer[3]) << 16) |
                 ((0xff & buffer[2]) << 8)  |
                 (0xff & buffer[1]);
		state = buffer[4];
		str = buffer[5];
		Stm32Updater.LOGGER.info("State: "+state+"; Status: "+status);
	}
	public byte getStatus() throws Exception {
	    /* Initialize the status data structure */
	    status = DfuStatus.DFU_STATUS_ERROR_UNKNOWN;
	    pollTimeout = 0;
	    state = DfuState.STATE_DFU_ERROR;
	    byte data[] = { 0x00, 0x00,0x00, 0x00 , 0x00, 0x00 };
	    
	    UsbControlIrp irp = device.createUsbControlIrp(
	    	    (byte) (UsbConst.ENDPOINT_DIRECTION_IN
	    	          | UsbConst.REQUESTTYPE_TYPE_CLASS
	    	          | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
	    	    DfuCommand.DFU_GETSTATUS,
	    	    (short) 0,
	    	    (short) 0
	    	    );
	    irp.setData(data);
	    irp.setLength(6);
	    device.syncSubmit(irp);
	    parseStatus(irp.getData());
	    return status;
	}
	public void clearStatus() throws Exception {
	    /* Initialize the status data structure */
	    status = DfuStatus.DFU_STATUS_ERROR_UNKNOWN;
	
	    UsbControlIrp irp = device.createUsbControlIrp(
	    	    (byte) (UsbConst.ENDPOINT_DIRECTION_OUT
	    	          | UsbConst.REQUESTTYPE_TYPE_CLASS
	    	          | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
	    	    DfuCommand.DFU_CLRSTATUS,
	    	    (short) 0,
	    	    (short) 0
	    	    );
	    device.syncSubmit(irp);
	}
	public void pollStatus(int state) throws Exception {
		do {
			Thread.sleep(1000);
			getStatus();
		} while(state == this.state);
		if (status != DfuStatus.DFU_STATUS_OK) {
			throw new Exception("pollStatus error");
		}
	}
	public void init() throws Exception {
		getStatus();
		if (state != DfuState.STATE_DFU_IDLE) {
			//clear status
			clearStatus();
			Thread.sleep(1000);
			getStatus();
			if (state == DfuState.STATE_DFU_ERROR) {
				throw new Exception("Unable to clear update state.");
			}
		}
		if (state != DfuState.STATE_DFU_IDLE) {
			throw new Exception("Device not ready to be updated.  Please replug USB connection.");
		}
	}
	public void dnloadSetAddress(long address) throws Exception {
		short wValue = 0x0000;
		byte data[] = new byte[5];
		data[0] = DfuCommand.DFU_DNLOAD_SET_ADDRESS;
		byte b[] = DfuUtils.getBytesfromInt(address);
		for (int i=0;i<4;i++) {
			data[i+1] = b[i];
		}
		dnload(wValue,data);
	}
	public void dnloadMassErase() throws Exception {
		short wValue = 0x0000;
		byte data[] = new byte[1];
		data[0] = DfuCommand.DFU_DNLOAD_ERASE;
		dnload(wValue,data);
	}
	public void dnloadWrite(short blockNum,byte[] data) throws Exception {
		dnload(blockNum,data);
	}	
	public void dnload(short wValue,byte[] data) throws Exception {
	    status = DfuStatus.DFU_STATUS_ERROR_UNKNOWN;
	    pollTimeout = 0;
	    state = DfuState.STATE_DFU_ERROR;
	    byte cmd = data[0];
	    if (wValue > 1) {
	    	cmd = DfuCommand.DFU_DNLOAD_WRITE;
	    }
	    
	    
	    if (cmd != DfuCommand.DFU_DNLOAD_ERASE && 
	    	cmd != DfuCommand.DFU_DNLOAD_SET_ADDRESS &&
	    	cmd != DfuCommand.DFU_DNLOAD_WRITE) {
	    	
	    	throw new Exception("Download Command not supported: "+data[0]);
	    }
	    
	    UsbControlIrp irp = device.createUsbControlIrp(
	    	    (byte) (UsbConst.ENDPOINT_DIRECTION_OUT
	    	          | UsbConst.REQUESTTYPE_TYPE_CLASS
	    	          | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
	    	    DfuCommand.DFU_DNLOAD,
	    	    wValue, (short) 0
	    	    );
	    
	    irp.setData(data);
	    irp.setLength(data.length);
	    device.syncSubmit(irp);
	    
	    getStatus();
	    if (state != DfuState.STATE_DFU_DOWNLOAD_BUSY) {
	    	throw new Exception("Error running download command: "+data[0]);
	    }
	    pollStatus(DfuState.STATE_DFU_DOWNLOAD_BUSY);
	}
}
