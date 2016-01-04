/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.io.EOFException;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Stm32Updater {
	private static Text text;
	public static ProgressBar progressBar = null;
	public static Display display = null;
	public static Shell shell = null;
	public static Button btnUpdate = null;
	public static Label lblProgress = null;
	public final static Logger LOGGER = Logger.getLogger(Stm32Updater.class
			.getName());
	public final static String productName = "UMotia";

	/**
	 * Launch the application.b
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Setup logger
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		try {
			FileHandler logFile = new FileHandler(productName + ".%u.%g.log",
					200000, 2, true);
			LOGGER.addHandler(logFile);
			logFile.setFormatter(formatter);
			logFile.setLevel(Level.CONFIG);
		} catch (IOException e) {
			System.err.println("Unable to create logfile");
			System.exit(3);
		}

		display = Display.getDefault();
		shell = new Shell();
		shell.setSize(450, 168);
		shell.setText(productName + " Updater");
		Button btnBrowse = new Button(shell, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressBar.setSelection(0);
				FileDialog fdlg = new FileDialog(shell, SWT.OPEN);
				String ext[] = { "*.dfu" };
				fdlg.setFilterExtensions(ext);
				String filename = fdlg.open();
				if (filename != null) {
					text.setText(filename);
					DfuseFile file = new DfuseFile();
					try {
						file.readFile(filename);
						btnUpdate.setEnabled(true);
					} catch (EOFException e1) {
						String msg = "File is not valid and could be empty";
						LOGGER.severe(msg);
						MessageDialog.openError(null, "Update File Error", msg);
					} catch (Exception e2) {
						LOGGER.severe(e2.getMessage());
						MessageDialog.openError(null, "Update File Error",
								e2.getMessage());
					}
				}
			}
		});
		btnBrowse.setBounds(308, 10, 75, 25);
		btnBrowse.setText("Browse...");

		text = new Text(shell, SWT.BORDER);
		text.setEditable(false);
		text.setBounds(85, 12, 217, 21);

		Label lblUpdateFile = new Label(shell, SWT.NONE);
		lblUpdateFile.setBounds(10, 15, 69, 15);
		lblUpdateFile.setText("Update File:");

		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(82, 51, 217, 17);

		btnUpdate = new Button(shell, SWT.NONE);
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnUpdate.setEnabled(false);
				new Thread() {
					public void run() {
						display.asyncExec(new Runnable() {
							public void run() {
								if (progressBar.isDisposed())
									return;
								DfuUpdater dfu = new DfuUpdater(text.getText());
								dfu.go(lblProgress, progressBar);
							}
						});
					}
				}.start();
				btnUpdate.setEnabled(true);
			}
		});
		btnUpdate.setBounds(111, 95, 108, 25);
		btnUpdate.setText("Update " + productName);
		btnUpdate.setEnabled(false);
		lblProgress = new Label(shell, SWT.NONE);
		lblProgress.setBounds(316, 53, 108, 15);

		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnExit.setBounds(240, 95, 75, 25);
		btnExit.setText("Exit");

		Label lblProgress_1 = new Label(shell, SWT.NONE);
		lblProgress_1.setAlignment(SWT.RIGHT);
		lblProgress_1.setText("Progress:");
		lblProgress_1.setBounds(0, 53, 69, 15);

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
