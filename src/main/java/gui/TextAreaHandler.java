package gui;

import javax.swing.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class TextAreaHandler extends Handler{
	private JTextArea textArea = null;

	private static TextAreaHandler handler = null;

	private TextAreaHandler() {
		LogManager manager = LogManager.getLogManager();
		String className = this.getClass().getName();
		String level = manager.getProperty(className + ".level");
		setLevel(level != null ? Level.parse(level) : Level.INFO);
		if (textArea == null)
			textArea = new JTextArea();
		
	}

	public void setTextArea(JTextArea textArea){
		this.textArea=textArea;
	}

	public static synchronized TextAreaHandler getInstance() {
		if (handler == null) {
			handler = new TextAreaHandler();
		}
		return handler;
	}

	public synchronized void publish(final LogRecord record) {
		String message = null;
		if (!isLoggable(record))
			return;

		message = getFormatter().format(record);
		textArea.append(message);
		textArea.repaint();
	}
	
	public void close() {
	}

	public void flush() {
	}
}
