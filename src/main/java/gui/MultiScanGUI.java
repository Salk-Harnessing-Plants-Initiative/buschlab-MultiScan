package gui;

import scan.Scanner;
import scan.UsbConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MultiScanGUI {
	private Logger log=Logger.getLogger(this.getClass().getName());
	private List<Scanner> scanners;
	private List<ScannerPanel> scannerPanels;
	List<ScanWorker> workers;
	private Thread scanThread;

	int scannersActive;
	private int nextPlateNr;
	
	private UsbConfig usbCfg;
	private String cfgPath;
	final private String newline=System.getProperty("line.separator");
	private JFrame frame;
	private JTextArea logArea;

	private LedLight ledMonitor;
	private JCheckBox[] chkRow;
	private JButton btnReset;
	private JButton btnExit;
	private JButton btnScan;
	private JButton btnFolderChoose;
	private JTextField txtFolder;
	private JTextField txtPrefix;
	
	/**
	 * Create the application.
	 */
	public MultiScanGUI(String cfgPath) {
		//			// look and feel
		//			try {
		//				// Set System L&F
		//				UIManager.setLookAndFeel(
		//						UIManager.getSystemLookAndFeelClassName());
		//			} 
		//			catch (UnsupportedLookAndFeelException e) {
		//				// handle exception
		//			}
		//			catch (ClassNotFoundException e) {
		//				// handle exception
		//			}
		//			catch (InstantiationException e) {
		//				// handle exception
		//			}
		//			catch (IllegalAccessException e) {
		//				// handle exception
		//			}
		//			
		this.cfgPath=cfgPath;
		this.nextPlateNr=1;
		this.scannerPanels=new ArrayList<ScannerPanel>();

		initializeFrame();
		Logger topLogger=Logger.getLogger("");

		TextAreaHandler handler=TextAreaHandler.getInstance();
		handler.setTextArea(logArea);
		handler.setLevel(Level.ALL);
		handler.setFormatter(new LogFormatter());

		
		boolean handlerIsSet=false;
		Handler[] curHandlers=topLogger.getHandlers();
		for(Handler h:curHandlers){
			if(h instanceof TextAreaHandler){
				handlerIsSet=true;
			}
		}
		if(!handlerIsSet)
			topLogger.addHandler(handler);


		log=Logger.getLogger(this.getClass().getName());

		dispatcher();
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeFrame(){
		frame = new JFrame("MultiScan V1.1");
		frame.setBounds(100, 100, 700, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.Y_AXIS));

		JPanel pnlMain = new JPanel();
		frame.getContentPane().add(pnlMain);
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.setPreferredSize(new Dimension(700,420));
//		pnlMain.setMaximumSize(new Dimension(800,Short.MAX_VALUE));

		GridBagConstraints gbc=new GridBagConstraints();
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.fill=GridBagConstraints.BOTH;
		for(int i=0;i<8;++i){
			gbc.gridx=i/4;
			gbc.gridy=i%4;
			ScannerPanel sp=new ScannerPanel(i+1);
//			if(scanners.get(i)==null){
//				sp.setActive(false);
//			}
//			sp.setNextPlateNr(nextPlateNr);
//			nextPlateNr++;
			sp.addItemListener(new CheckBoxHandler());
			scannerPanels.add(sp);
			pnlMain.add(sp.getPanel(),gbc);
		}
		
		
		new JPanel();
		
		initializeCrtlPanel();
		initializeLogPanel();
//		initializeBaseTab();
//		initializeClassifierTab();
//		initializePhenotypeTab();
		frame.setVisible(true);
		
	}

	private void initializeCrtlPanel(){
		JPanel pnlControl=new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.weightx=0.0;
//		gbc.weighty=0.0;
		JLabel lblMonitor=new JLabel("Monitor");
		gbc.gridx=0;
		gbc.gridy=0;
		pnlControl.add(lblMonitor,gbc);
		
		ledMonitor=new LedLight(Color.green);
		ledMonitor.off();
		ledMonitor.setPreferredSize(new Dimension(13,13));
		gbc.gridx=0;
		gbc.gridy=1;
		pnlControl.add(ledMonitor,gbc);
		
		chkRow=new JCheckBox[2];
		gbc.weightx=0.3;
//		gbc.weighty=0.1;
		gbc.gridx=1;
		gbc.anchor=GridBagConstraints.EAST;
		for(int i=0;i<2;++i){
			gbc.gridy=i;
			chkRow[i]=new JCheckBox("Row "+(i+1));
			chkRow[i].setSelected(true);
			pnlControl.add(chkRow[i],gbc);
		}
		
		JLabel lblFolder=new JLabel("Folder:");
		gbc.weightx=0.3;
		gbc.gridx=2;
		gbc.gridy=0;
		gbc.anchor=GridBagConstraints.EAST;
		pnlControl.add(lblFolder,gbc);
		
		txtFolder=new JTextField(System.getProperty("user.home"));
		gbc.gridx=3;
		gbc.gridy=0;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.CENTER;
		pnlControl.add(txtFolder,gbc);
		
		btnFolderChoose=new JButton("...");
		btnFolderChoose.setPreferredSize(new Dimension(25,25));
		gbc.gridx=4;
		gbc.gridy=0;
		gbc.fill=GridBagConstraints.NONE;
		pnlControl.add(btnFolderChoose,gbc);
		
		JLabel lblPrefix=new JLabel("Prefix:");
		gbc.weightx=0.9;
//		gbc.weighty=0.9;
		gbc.gridx=2;
		gbc.gridy=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		pnlControl.add(lblPrefix,gbc);
		
		txtPrefix=new JTextField("_set1_day1");
		gbc.gridx=3;
		gbc.gridy=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.CENTER;
		pnlControl.add(txtPrefix,gbc);
		
		btnReset=new JButton("Reset");
		btnReset.setPreferredSize(new Dimension(80,25));
		gbc.weightx=0.1;
//		gbc.weighty=0.1;
		gbc.gridx=5;
		gbc.gridy=0;
		gbc.fill=GridBagConstraints.NONE;
		pnlControl.add(btnReset,gbc);
		
		btnExit=new JButton("Exit");
		btnExit.setPreferredSize(new Dimension(80,25));
		gbc.gridx=5;
		gbc.gridy=1;
		pnlControl.add(btnExit,gbc);

		btnScan=new JButton("Scan");
		btnScan.setPreferredSize(new Dimension(80,50));
		gbc.gridx=6;
		gbc.gridy=0;
		gbc.gridheight=2;
		gbc.fill=GridBagConstraints.VERTICAL;
		pnlControl.add(btnScan,gbc);

		pnlControl.setBorder(new TitledBorder("Control"));
		frame.getContentPane().add(pnlControl);
	}
	
	private void initializeLogPanel(){
		JPanel logPnl = new JPanel();
		frame.getContentPane().add(logPnl);
		logPnl.setLayout(new BorderLayout(0, 0));

		logArea=new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font("Monospaced",Font.PLAIN,10));
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);		//logArea.setPreferredSize(new Dimension(400,Short.MAX_VALUE));
		JScrollPane logPane=new JScrollPane(logArea);
		logPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		logPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logPane.setBorder(new TitledBorder("Log Area"));
		logPane.setPreferredSize(new Dimension(400,300)); //Short.MAX_VALUE));

		logPnl.add(logPane, BorderLayout.CENTER);

		JPanel pnlLogAction = new JPanel();
		logPnl.add(pnlLogAction, BorderLayout.SOUTH);

		JButton btnWriteLog = new JButton("Write Log");
		pnlLogAction.add(btnWriteLog);


	}
	
	public void updateScanners(){
//		scannersActive=0;
		for(int i=0;i<8;++i){
			if(scanners.get(i)==null){
				scannerPanels.get(i).setActive(false);
			}
			else{
				scannerPanels.get(i).setActive(true);
				scanners.get(i).addProcessingListener(new PropertyChangeHandler());
				scanners.get(i).addProgressListener(new PropertyChangeHandler());
//				if(scannerPanels.get(i).isActive()){
//					
//				}
			}
		}
	}
	
	
	private void dispatcher(){
		usbCfg=new UsbConfig(cfgPath);
		for(int i=0;i<2;++i){
			if(!usbCfg.rowEnabled(i)){
				chkRow[i].setSelected(false);
				chkRow[i].setEnabled(false);
			}
		}
		
		usbCfg.addMonitorListener(new PropertyChangeHandler());
		
		usbCfg.addConfigListener(new PropertyChangeHandler());
		
		btnExit.addActionListener(new ActionHandler());
		btnReset.addActionListener(new ActionHandler());
		btnScan.addActionListener(new ActionHandler());
		btnFolderChoose.addActionListener(new ActionHandler());
		
		usbCfg.start();

		txtPrefix.getDocument().addDocumentListener(new DocumentHandler());
//		new Thread(){
//			public void run(){
//		try {
//			Thread.sleep(3000);
//			usbCfg.interrupt();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//			}
//		}.start();
		
//		while(true){}
	}
	
	private void reset(){
		new Thread(){
			public void run(){
//				if(workers!=null){
//					for(int i=0;i<workers.size();++i){
//						workers.get(i).interrupt();
//					}
//				}
				if(scanThread!=null)
					scanThread.interrupt();
				usbCfg.interrupt();
				try {
					for(int i=0;i<8;++i){
						if(scanners.get(i)!=null){
							scanners.get(i).destroyProcess();
							Thread.sleep(200);
						}
						scannerPanels.get(i).ledOnlineOff();
						scannerPanels.get(i).ledBusyOff();
						Thread.sleep(100);
					}
					usbCfg.join(500);

					Thread.sleep(300);
					
					usbCfg=new UsbConfig(cfgPath);
					usbCfg.addMonitorListener(new PropertyChangeHandler());
					usbCfg.addConfigListener(new PropertyChangeHandler());
					usbCfg.start();
					nextPlateNr=1;
					updateFileFields();
//						if(scannerPanels.get(i).isActive()){
//							scannerPanels.get(i).setNextPlateNr(nextPlateNr);
//							scannerPanels.get(i).updateFileField(txtPrefix.getText());
//							nextPlateNr++;
//						}
////						scannerPanels.get(i).resetNextPlateNr();
////						scannerPanels.get(i).updateFileField(txtPrefix.getText());
//					}
//					for(int i=0;i<workers.size();++i){
//							workers.get(i).join(500);
//					}
					scanThread.join();
					for(int i=0;i<8;++i){
						scannerPanels.get(i).setProgress(0);
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}.start();
	}
	
	public void scan(){
		boolean scannerAvail=false;
		for(int i=0;i<8;++i){
			if(scanners.get(i)!=null){
				scannerAvail=true;
				break;
			}
		}
		if(!scannerAvail){
			log.info("No scanners available!!");
			return;
		}
		scanThread=new Thread(){
			public void run(){
				List<ScanWorker> workers=null;
				try {
					if(chkRow[0].isSelected()){
						updateFileFields();
						workers=new ArrayList<ScanWorker>();
						for(int i=0;i<8;++i){
							if(scanners.get(i)!=null && scannerPanels.get(i).isActive()){
								ScanWorker scanWorker=new ScanWorker(scanners.get(i),0,txtFolder.getText()+System.getProperty("file.separator")+scannerPanels.get(i).getFileName()+".tif");
								scanWorker.start();
								workers.add(scanWorker);
								Thread.sleep(500);
							}
						}
						for(int i=0;i<workers.size();++i){
							workers.get(i).join();
						}		
						Thread.sleep(3000);

						for(int i=0;i<8;++i){
							//						scannerPanels.get(i).setNextPlateNr(scannerPanels.get(i).getNextPlateNr()+scannersActive);//+scanners.size());
							//						scannerPanels.get(i).updateFileField(txtPrefix.getText());
							//scannerPanels.get(i).setProgress(0);
							if(scannerPanels.get(i).isActive()){
								nextPlateNr++;
							}
						}
						updateFileFields();
					}

					if(chkRow[1].isSelected() && !isInterrupted()){
						updateFileFields();
						workers=new ArrayList<ScanWorker>();
						for(int i=0;i<8;++i){

							if(scanners.get(i)!=null && scannerPanels.get(i).isActive()){
								ScanWorker scanWorker=new ScanWorker(scanners.get(i),1,txtFolder.getText()+System.getProperty("file.separator")+scannerPanels.get(i).getFileName()+".tif");
								scanWorker.start();
								workers.add(scanWorker);
								Thread.sleep(500);
							}
						}
						for(int i=0;i<workers.size();++i){
							workers.get(i).join();
						}
						for(int i=0;i<8;++i){
							//						scannerPanels.get(i).setNextPlateNr(scannerPanels.get(i).getNextPlateNr()+scannersActive);//+scanners.size());
							//						scannerPanels.get(i).updateFileField(txtPrefix.getText());
							//scannerPanels.get(i).setProgress(0);
							if(scannerPanels.get(i).isActive()){
								nextPlateNr++;
							}
						}
						updateFileFields();
					}
				}
				catch(InterruptedException ie){
					if(workers!=null){
						for(ScanWorker w:workers){
							w.interrupt();
//							try {
//								w.join();
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
						}
					}
					//return;
				}
				finally{
					for(int i=0;i<8;++i){
						scannerPanels.get(i).setProgress(0);
					}
				}
			}

		};
		scanThread.start();
	}
	
	private void updateFileFields(){
		int activeCnt=-1;
		for(int i=0;i<8;++i){
			if(scannerPanels.get(i).isActive()){
				activeCnt++;
				scannerPanels.get(i).setNextPlateNr(nextPlateNr+activeCnt);//+scanners.size());
			}
			scannerPanels.get(i).updateFileField(txtPrefix.getText());
//			scannerPanels.get(i).setProgress(0);
		}
		
	}
	
	private class ScanWorker extends Thread{
		Scanner scanner;
		int row;
		String filepath;
		
		public ScanWorker(Scanner scanner,int row,String filepath){
			this.scanner=scanner;
			this.row=row;
			this.filepath=filepath;
			
		}
		
		public void run(){
			int returnValue=999;
			int retryCnt=1;
			while(returnValue!=0 && retryCnt<=10 && !isInterrupted()){
				log.info("Scanner "+scanner.getLogical()+": starting to scan row "+row);
				returnValue=scanner.scanImage(row,filepath);
				log.info("Scanning failed on scanner"+scanner.getLogical()+"\n"+
						 "Will retry in 2 seconds ("+retryCnt+"/10)");
				retryCnt++;
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					log.info("scan worker "+scanner.getLogical()+" was interrupted");
					return;
					//e.printStackTrace();
				}
				
			}
		}
	}
	
	private class PropertyChangeHandler implements PropertyChangeListener{

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("monitoringScanners")){
				if((Boolean)evt.getNewValue()){
					ledMonitor.on();
				}
				else{
					ledMonitor.off();
				}
			}
			else if(evt.getPropertyName().equals("ScannerConfigChanged")){
				scanners=(List<Scanner>)evt.getNewValue();
				updateScanners();
				updateFileFields();
			}
			else if(evt.getPropertyName().equals("ScanProcessing")){
				scannerPanels.get((Integer)evt.getNewValue()).ledBusyOn();
			}
			else if(evt.getPropertyName().equals("ScanFinished")){
				scannerPanels.get((Integer)evt.getNewValue()).ledBusyOff();
			}
			else if(evt.getPropertyName().equals("ProgressChanged")){
				int pnlNr=(Integer)evt.getOldValue();
				scannerPanels.get(pnlNr).setProgress((Double)evt.getNewValue());
			}
		}
	}
	
	private class ActionHandler implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==btnExit){
				usbCfg.interrupt();
				for(int i=0;i<8;++i){
					if(scanners.get(i)!=null){
						scanners.get(i).destroyProcess();
					}
				}
				frame.dispose();
				System.exit(0);
			}
			else if(e.getSource()==btnReset){
				reset();
			}
			else if(e.getSource()==btnScan){
				scan();
			}
			else if(e.getSource()==btnFolderChoose){
				JFileChooser fc=new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
				int state=fc.showOpenDialog(null);
				if(state==JFileChooser.APPROVE_OPTION){
					File file=fc.getSelectedFile();
					if(file.isDirectory()){
						txtFolder.setText(file.getAbsolutePath());
					}
					else{
						log.info("selected file is not a directory");
					}
				}
				
			}
		}
		
	}
	
	private class DocumentHandler implements DocumentListener{

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateImageFileNames();			
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateImageFileNames();
			
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		private void updateImageFileNames(){
			for(int i=0;i<8;++i){
				scannerPanels.get(i).updateFileField(txtPrefix.getText());
			}
		}
		
	}
	
	private class CheckBoxHandler implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent e) {
			updateImageFileNames();
		}
		private void updateImageFileNames(){
//			int scannersActive=0;
//			for(int i=0;i<8;++i){
//				if(scannerPanels.get(i).isActive()){
//					scannersActive++;
//				}
//				nextPlateNr-=scannersActive;
				updateFileFields();
//				scannerPanels.get(i).updateFileField(txtPrefix.getText());
//			}
		}
		
	}

}


