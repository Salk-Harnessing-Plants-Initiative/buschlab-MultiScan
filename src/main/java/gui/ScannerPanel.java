package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScannerPanel {
	private int scannerNr;
	private int nextPlateNr;
	private JPanel pnlScan;
	
	private LedLight ledOnline;
	private LedLight ledBusy;
	
	private JProgressBar progressBar;
	private JTextField txtFile;
	
	private JCheckBox chkUseScanner;
	
	public ScannerPanel(int scannerNr){
		this.scannerNr=scannerNr;
		nextPlateNr=scannerNr;
		init();
	}
	
	
	public JPanel getPanel(){
		return pnlScan;
	}
	
	public int getNextPlateNr(){
		return nextPlateNr;
	}
	
	public void setNextPlateNr(int nextPlateNr){
		this.nextPlateNr=nextPlateNr;
	}
	
	public void resetNextPlateNr(){
		this.nextPlateNr=scannerNr;
	}
	private void init(){
		JLabel lblOnline=new JLabel(" Online ");
		JLabel lblBusy=new JLabel(" Busy ");
		ledOnline=new LedLight(Color.green);
		ledOnline.setPreferredSize(new Dimension(13,13));
		ledOnline.setMaximumSize(new Dimension(13,13));
		ledBusy=new LedLight(Color.red);
		ledBusy.setPreferredSize(new Dimension(13,13));
		ledBusy.setMaximumSize(new Dimension(13,13));
		ledOnline.off();
		ledBusy.off();
		
		JPanel pnlLeds = new JPanel(new GridBagLayout());
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=0.0;
		gbc.weighty=0.0;
//		gbc.fill=GridBagConstraints.VERTICAL;
		pnlLeds.add(lblOnline,gbc);
		gbc.gridx=1;
		gbc.gridy=0;
		pnlLeds.add(lblBusy,gbc);
		gbc.gridx=0;
		gbc.gridy=1;
		pnlLeds.add(ledOnline,gbc);
		gbc.gridx=1;
		gbc.gridy=1;
		pnlLeds.add(ledBusy,gbc);
		
		JLabel lblUseScanner=new JLabel("Use:");
		gbc.gridx=0;
		gbc.gridy=2;
		//gbc.anchor=GridBagConstraints.EAST;
		pnlLeds.add(lblUseScanner,gbc);
		chkUseScanner=new JCheckBox();
		chkUseScanner.setSelected(true);
		gbc.gridx=1;
		gbc.gridy=2;
		gbc.anchor=GridBagConstraints.CENTER;
		pnlLeds.add(chkUseScanner,gbc);
		
		JLabel lblProgress=new JLabel("Progress");
		progressBar=new JProgressBar(0,100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		gbc.gridx=2;
		gbc.gridy=0;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.anchor=GridBagConstraints.WEST;
		pnlLeds.add(lblProgress,gbc);
		gbc.gridx=2;
		gbc.gridy=1;
		gbc.gridwidth=2;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.CENTER;
		pnlLeds.add(progressBar,gbc);

//		JLabel lblFile=new JLabel("File: ");
//		gbc.gridx=1;
//		gbc.gridy=2;
//		gbc.weightx=0.3;
//		gbc.gridwidth=1;
//		gbc.anchor=GridBagConstraints.EAST;
//		pnlLeds.add(lblFile,gbc);
		
		txtFile=new JTextField(20);
		txtFile.setHorizontalAlignment(JTextField.CENTER);
		updateFileField("default_set1_day1");
		gbc.gridx=2;
		gbc.gridy=2;
//		gbc.gridwidth=2;
		gbc.weightx=1.0;
		gbc.anchor=GridBagConstraints.CENTER;
		pnlLeds.add(txtFile,gbc);
		
		pnlScan=new JPanel(new GridLayout(1,2));
		pnlScan.setBorder(new TitledBorder("Scanner "+scannerNr));
		pnlScan.add(pnlLeds);
	}
	
	public void updateFileField(String prefix){
		SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd-HHmmss");
		Date date=new Date();
		if(chkUseScanner.isSelected())
			txtFile.setText(prefix+"_"+df.format(date)+"_"+String.format("%03d",nextPlateNr));
		else
			txtFile.setText("");
	}
	
	public void addItemListener(ItemListener il){
		chkUseScanner.addItemListener(il);
	}
	
	public void ledOnlineOff(){
			ledOnline.off();
	}
	
	public void ledOnlineOn(){
		ledOnline.on();
	}
	
	public void ledBusyOff(){
			ledBusy.off();
	}
	
	public void ledBusyOn(){
		ledBusy.on();
	}
	public void setActive(boolean active){
		if(!active){
			ledOnlineOff();
			ledBusyOff();
			progressBar.setEnabled(false);
			chkUseScanner.setSelected(false);
			chkUseScanner.setEnabled(false);
		}
		else{
			ledOnlineOn();
			progressBar.setEnabled(true);
//			chkUseScanner.setSelected(true);
			chkUseScanner.setEnabled(true);
			
		}
	}
	
	public boolean isActive(){
		return chkUseScanner.isSelected();
	}
	
	public String getFileName(){
		return txtFile.getText();
	}
	
	public void setProgress(double progress){
		progressBar.setValue((int)progress);
	}
}
