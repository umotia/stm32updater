/*
 * Copyright (C) 2015 Norman James (nkskjames@gmail.com)
 * See LICENSE.md file for copying conditions
 */

package com.umotia.updater;

import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
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
	private static Shell shell;
	public static Button btnUpdate = null;
	public static Label lblProgress = null;
	public final static Logger LOGGER = Logger.getLogger(Stm32Updater.class
			.getName());
	public static ResourceBundle messages;

	/**
	 * Launch the application.b
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Locale currentLocale = new Locale("en", "US");
		messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);

		setupLogging();
		createDialog();
	}

	public static void createDialog() {
		display = Display.getDefault();
		shell = new Shell();
		shell.setSize(458, 193);
		shell.setText(messages.getString("product_name"));
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
						lblProgress.setText(messages.getString("text_click_update"));
						btnUpdate.setEnabled(true);
					} catch (EOFException e1) {
						LOGGER.severe(messages.getString("message_file_not_valid"));
						MessageDialog.openError(null,
								messages.getString("title_file_error"),
								messages.getString("message_file_not_valid"));
					} catch (Exception e2) {
						LOGGER.severe(e2.getMessage());
						MessageDialog.openError(null,
								messages.getString("title_file_error"),
								e2.getMessage());
					}
				}
			}
		});
		btnBrowse.setBounds(338, 46, 75, 25);
		btnBrowse.setText(messages.getString("button_browse"));

		text = new Text(shell, SWT.BORDER);
		text.setEditable(false);
		text.setBounds(85, 48, 247, 21);

		Label lblUpdateFile = new Label(shell, SWT.NONE);
		lblUpdateFile.setAlignment(SWT.RIGHT);
		lblUpdateFile.setBounds(10, 51, 69, 15);
		lblUpdateFile.setText(messages.getString("label_update_file"));

		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(85, 84, 247, 17);

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
		btnUpdate.setBounds(338, 81, 75, 25);
		btnUpdate.setText("Do Update");
		btnUpdate.setEnabled(false);
		lblProgress = new Label(shell, SWT.NONE);
		lblProgress.setText(messages.getString("text_click_browse"));
		lblProgress.setBounds(85, 15, 247, 15);

		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnExit.setBounds(179, 120, 75, 25);
		btnExit.setText("Exit");

		Label lblProgress_2 = new Label(shell, SWT.NONE);
		lblProgress_2.setText(messages.getString("label_progress"));
		lblProgress_2.setAlignment(SWT.RIGHT);
		lblProgress_2.setBounds(10, 86, 69, 15);

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static void setupLogging() {
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		try {
			FileHandler logFile = new FileHandler(
					messages.getString("product_name") + ".%u.%g.log", 200000,
					2, true);
			LOGGER.addHandler(logFile);
			logFile.setFormatter(formatter);
			logFile.setLevel(Level.CONFIG);
		} catch (IOException e) {
			System.err.println(messages.getString("message_log_error"));
			System.exit(3);
		}
	}
}
