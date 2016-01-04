/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.util.List;
import java.util.Vector;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class DfuUpdater {
	private String filename = "";
	
	public DfuUpdater(String filename) {
		this.filename = filename;
	}
	@SuppressWarnings("unchecked")
	public static UsbDevice findDevice(UsbHub hub, short vendorId,
			short productId) {
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			if (desc.idVendor() == vendorId && desc.idProduct() == productId)
				return device;
			if (device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if (device != null)
					return device;
			}
		}
		return null;
	}
	
	public void go(Label lblProgress, ProgressBar progressBar) {
		DfuseFile file = new DfuseFile();
		try {
			lblProgress.setText("Reading file");
			file.readFile(filename);
		} catch (Exception e1) {
			lblProgress.setText("Error");
			Stm32Updater.LOGGER.severe(e1.getMessage());
			MessageDialog.openError(null, "Error", e1.getMessage());
			return;
		}

		try {
			UsbDevice device = getUsbDevice(file.getVendor(), file.getProduct());
			Vector<DfuseImage> images = file.getImages();

			for (DfuseImage image : images) {
				download(lblProgress, progressBar, device, image);
			}
		} catch (Exception e1) {
			lblProgress.setText("Error");
			MessageDialog.openError(null, "Error", e1.getMessage());
			Stm32Updater.LOGGER.severe(e1.getMessage());
		}
	}

	public UsbDevice getUsbDevice(short vendor, short product) throws Exception {
		UsbServices services = UsbHostManager.getUsbServices();
		UsbHub hub = services.getRootUsbHub();
		UsbDevice device = DfuUpdater.findDevice(hub, vendor, product);
		if (device == null) {
			throw new Exception(
					String.format(
							"USB device not found for Vendor = 0x%04X and Product = 0x%04X",
							vendor, product));
		}
		return device;
	}
	public void download(Label lblProgress, ProgressBar progressBar, UsbDevice device, DfuseImage image) {
		try {
			UsbConfiguration configuration = device.getActiveUsbConfiguration();
			UsbInterface iface = configuration.getUsbInterface((byte) 0)
					.getSetting(image.getAlt());
			DfuseMemoryLayout memLayout = new DfuseMemoryLayout();
			memLayout.parse(iface.getInterfaceString());
			iface.claim();
			DfuDriver dfu = new DfuDriver(device);
			try {
				dfu.init();
				lblProgress.setText("Erasing");
				dfu.dnloadMassErase();

				int numImages = image.getElements().size();
				int imageNum = 1;
				for (DfuseElement element : image.getElements()) {
					lblProgress.setText("Flashing "+imageNum+" of "+numImages);
					progressBar.setMinimum(0);
					progressBar.setSelection(0);
					
					DfuseMemoryEntry memEntry = memLayout
							.getEntry(element.address);
					dfu.dnloadSetAddress(element.address);

					byte data[] = new byte[memEntry.pageSize];
					int numBlocks = (int) (element.elementSize / memEntry.pageSize);
					int leftover = (int) (element.elementSize % memEntry.pageSize);

					int numBlkChk = numBlocks;
					if (leftover > 0) {
						numBlkChk++;
					}
					if (numBlkChk > memEntry.numSectors) {
						Stm32Updater.LOGGER.info(memEntry.toString());
						throw new Exception("Flash file has too many blocks");
					}
					progressBar.setMaximum(numBlocks+1);
					for (int b = 0; b < numBlocks; b++) {
						for (int i = 0; i < data.length; i++) {
							data[i] = element.data[b * memEntry.pageSize + i];
						}
						progressBar.setSelection(b+1);
						dfu.dnloadWrite((short) (b + 2), data);
					}
					if (leftover > 0) {
						data = new byte[leftover];
						for (int i = 0; i < leftover; i++) {
							data[i] = element.data[numBlocks
									* memEntry.pageSize + i];
						}
						dfu.dnloadWrite((short) (numBlocks + 2), data);
						progressBar.setSelection(numBlocks+1);
					}
					imageNum++;
				}
				dfu.clearStatus();
				lblProgress.setText("Complete");
				String msg = "The firmware update operation is complete";
				Stm32Updater.LOGGER.info(msg);
				MessageDialog.openInformation(null, "Update Operation",msg);
			} catch (Exception e) {
				Stm32Updater.LOGGER.severe(e.getMessage());
				MessageDialog.openError(null, "Error", e.getMessage());
			} finally {
				iface.release();
			}
		} catch (Exception e) {
			Stm32Updater.LOGGER.severe(e.getMessage());
			MessageDialog.openError(null, "Error", e.getMessage());
		}
	}
}
