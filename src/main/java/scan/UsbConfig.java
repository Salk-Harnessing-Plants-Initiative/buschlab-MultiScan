package scan;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsbConfig extends Thread{
	final private Logger log=Logger.getLogger(this.getClass().getName());
	private Map<String,Integer> scannerAssignment;
	private String vendorString;
	private String productString;
	private String driver;
	private Rectangle[] range;
//	private Rectangle range2;
	private Integer resolution;
	private String format;
	private String[] alternative;
	
	private List<List<String>> usbDevices;
	private List<Integer> scanDevices;
	private List<Scanner> scanners;
	private List<Scanner> prevScanners;
	
	private PropertyChangeSupport monitoring;
	private PropertyChangeSupport cfgChanged;
	
	public UsbConfig(InputStream cfgStream){
//		this.log=log;
		usbDevices=new ArrayList<List<String>>();
		scanDevices=new ArrayList<Integer>();
		scanners=new ArrayList<Scanner>();
		range=new Rectangle[2];
		
		cfgChanged=new PropertyChangeSupport(this);
		monitoring=new PropertyChangeSupport(this);
		readUsbConfig(cfgStream);
		if(scannerAssignment==null){
			setDefaultConfig();
		}
	}
	
	@Override
	public void run(){
		monitoring.firePropertyChange("monitoringScanners",false,true);
		log.info("Monitoring the scanner states...");
		while(!isInterrupted()){
			getConfig();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				interrupt();
				monitoring.firePropertyChange("monitoringScanners",true,false);
				log.info("Scanner montior was stopped!");
			}
		}
	}
	
	public List<Scanner> getScanners(){
		return scanners;
	}
	
	public void sortScanners(){
		List<Scanner> sortedScanners=new ArrayList<Scanner>();
		for(int i=0;i<8;++i){
			sortedScanners.add(null);
		}
		
		for(Scanner scanner:scanners){
			String hash=scanner.getHash();
			Integer logical=null;
			if(scannerAssignment.containsKey(hash)){
				logical=scannerAssignment.get(hash);
				scanner.setLogical(logical-1);
				sortedScanners.set(logical-1,scanner);
			}
		}
		scanners=sortedScanners;
	}
	
	public void getConfig(){
		prevScanners=scanners;
		usbDevices=new ArrayList<List<String>>();
		scanDevices=new ArrayList<Integer>();
		scanners=new ArrayList<Scanner>();
		String s = null;

		try {

			// using the Runtime exec method:
			Process p = Runtime.getRuntime().exec("usb-devices");
//			Process p = Runtime.getRuntime().exec("cat /home/GMI/christian.goeschl/usb-devices-gmi-lws10.txt");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			List<String> usbDeviceCfg=new ArrayList<String>();
			while ((s = stdInput.readLine()) != null) {
				if(s.equals("")){
					if(usbDeviceCfg.size()!=0){
						usbDevices.add(usbDeviceCfg);
					}
					usbDeviceCfg=new ArrayList<String>();
				}
				else{
					usbDeviceCfg.add(s);
					if(s.contains(vendorString) && s.contains(productString)){
						scanDevices.add(usbDevices.size());
					}
				}
			}
			if(usbDeviceCfg.size()!=0){
				usbDevices.add(usbDeviceCfg);
			}
			
			// read any errors from the attempted command
//			System.out.println("Here is the standard error of the command (if any):\n");
//			while ((s = stdError.readLine()) != null) {
//				System.out.println(s);
//			}
			
			Pattern busPattern=Pattern.compile("Bus=\\d+");
			Pattern portPattern=Pattern.compile("Port=\\d+");
			Pattern devPattern=Pattern.compile("Dev#=\\s+\\d+");
			for(Integer devNr:scanDevices){
				List<String> scannerCfg=usbDevices.get(devNr);
				int bus=-1;
				int port=-1;
				int dev=-1;
				for(String line:scannerCfg){
					Matcher m=busPattern.matcher(line);
					if(m.find()){
						String grp=m.group();
						Matcher mint=Pattern.compile("\\d+").matcher(grp);
						if(mint.find()){
							bus=Integer.parseInt(mint.group());
						}
					}
					m=portPattern.matcher(line);
					if(m.find()){
						String grp=m.group();
						Matcher mint=Pattern.compile("\\d+").matcher(grp);
						if(mint.find()){
							port=Integer.parseInt(mint.group());
						}
					}
					m=devPattern.matcher(line);
					if(m.find()){
						String grp=m.group();
						Matcher mint=Pattern.compile("\\d+").matcher(grp);
						if(mint.find()){
							dev=Integer.parseInt(mint.group());
						}
					}
				}
				if(alternative!=null){
					scanners.add(new Scanner(bus,port,dev,driver,alternative));
				}
				else{
					scanners.add(new Scanner(bus,port,dev,driver,resolution,range[0],range[1],format));
				}
			}
			
			sortScanners();
			
			if(configChanged()){
				log.info("scanner configuration changed");
				cfgChanged.firePropertyChange("ScannerConfigChanged",prevScanners,scanners);
			}
//			System.out.println("Got configuration of "+usbDevices.size()+" devices.");
//			for(int i=0;i<usbDevices.size();++i){
//				System.out.print("Device "+i);
//				if(scanners.contains(i)){
//					System.out.println(" (Scanner):");
//				}
//				else{
//					System.out.println(":");
//				}
//				for(int j=0;j<usbDevices.get(i).size();++j){
//					System.out.println("\t"+usbDevices.get(i).get(j));
//				}
//			}
//			System.exit(0);
		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void readUsbConfig(InputStream cfgStream){
		scannerAssignment=new HashMap<String,Integer>();

		BufferedReader br=null;
		String logStr="searching for usb configuration: '"+System.getProperty("line.separator");
		
		try{
			br=new BufferedReader(new InputStreamReader(cfgStream));

			String line;
			while((line=br.readLine())!=null){
				line=line.trim();
				if(line.startsWith("#") || line.isEmpty())
					continue;
				String[] lineParts=line.split("\\s+");

				switch(lineParts[0]){
				case "scanner:":
					Integer scannerNr=Integer.parseInt(lineParts[1]);
					String scannerPort=lineParts[2];
					scannerAssignment.put(scannerPort,scannerNr);
					break;
				case "vendor:":
					vendorString=lineParts[1];
					productString=lineParts[2];
					break;
				case "range1:":
					range[0]=new Rectangle(Integer.parseInt(lineParts[1]),Integer.parseInt(lineParts[2]),
										   Integer.parseInt(lineParts[3]),Integer.parseInt(lineParts[4]));
					break;
				case "range2:":
					range[1]=new Rectangle(Integer.parseInt(lineParts[1]),Integer.parseInt(lineParts[2]),
										   Integer.parseInt(lineParts[3]),Integer.parseInt(lineParts[4]));
					break;
				case "resolution:":
					resolution=Integer.parseInt(lineParts[1]);
					break;
				case "format:":
					format=lineParts[1];
					break;
				case "driver:":	
					driver=lineParts[1];
					break;
				case "override1:":
					if(alternative==null){
						alternative=new String[2];
					}
					alternative[0]="";
					for(int i=1;i<lineParts.length;++i){
						alternative[0]+=lineParts[i]+" ";
					}
					break;
				case "override2:":
					if(alternative==null){
						alternative=new String[2];
					}
					alternative[1]="";
					for(int i=1;i<lineParts.length;++i){
						alternative[1]+=lineParts[i]+" ";
					}
					break;
				}
			}

			for(Map.Entry<String,Integer> scannerEntry:scannerAssignment.entrySet()){
				logStr+="    Scanner "+scannerEntry.getValue()+" at port "+scannerEntry.getKey()+System.getProperty("line.separator");
			}
			if(alternative!=null){
				logStr+="    Scanner options set by 'override' option."+System.getProperty("line.separator");
			}
		}
		catch (FileNotFoundException e) {
			logStr+="    configuration not found!"+System.getProperty("line.separator");
			logStr+="    using default config."+System.getProperty("line.separator");
			scannerAssignment=null;
		}
		catch (IOException e) {
			logStr+="    error reading configuration!"+System.getProperty("line.separator");
			logStr+="    using default config."+System.getProperty("line.separator");
			scannerAssignment=null;
		}
		catch(Exception e){
			logStr+="    syntax error in configuration file!"+System.getProperty("line.separator");
			logStr+="    using default config."+System.getProperty("line.separator");
			scannerAssignment=null;
		}
		finally{
			try {
				if(br!=null)
					br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info(logStr);
		}
	}
	
	public boolean rowEnabled(int row){
		return range[row]!=null;
	}
	private void setDefaultConfig(){
		vendorString="04b8";
		productString="013a";
		
		scannerAssignment=new HashMap<String,Integer>();
		scannerAssignment.put("2.7",1);
		scannerAssignment.put("2.4",2);
		scannerAssignment.put("2.5",3);
		scannerAssignment.put("1.3",4);
		scannerAssignment.put("1.5",5);
		scannerAssignment.put("1.2",6);
		scannerAssignment.put("1.1",7);
		scannerAssignment.put("1.0",8);
		range[0]=new Rectangle(150,40,140,140);		
		range[1]=new Rectangle(5,40,140,140);	
		resolution=1200;	
		format="tiff";
		driver="epkowa:interpreter";
	}
	
	private boolean configChanged(){
		if(scanners.size()!=prevScanners.size()){
			return true;
		}
		
		for(int i=0;i<scanners.size();++i){
			if((scanners.get(i)==null && prevScanners.get(i)!=null) || (scanners.get(i)!=null && prevScanners.get(i)==null)){
				return true;
			}
			if(scanners.get(i)==null || prevScanners.get(i)==null)
				continue;
			if(!scanners.get(i).getHash().equals(prevScanners.get(i).getHash())){
				return true;
			}
		}
		return false;
	}
	
	public void addConfigListener(PropertyChangeListener l){
		cfgChanged.addPropertyChangeListener(l);
	}
	
	public void removeConfigListener(PropertyChangeListener l){
		cfgChanged.removePropertyChangeListener(l);
	}
	
	public void addMonitorListener(PropertyChangeListener l){
		monitoring.addPropertyChangeListener(l);
	}
	
	public void removeMonitorListener(PropertyChangeListener l){
		monitoring.removePropertyChangeListener(l);
	}
	
//	public void getConfig() throws IOException{
//		usbCfgStrings=new ArrayList<String>();
//		File usbCfgFile=new File("/sys/kernel/debug/usb/devices");
//
//		FileReader fr=null;
//		BufferedReader br=null;
//		try{
//			fr=new FileReader(usbCfgFile);
//			br=new BufferedReader(fr);
//			
//			String line;
//			
//			while((line=br.readLine())!=null){
//				usbCfgStrings.add(line);
//			}
//		} catch (IOException e) {
//			System.err.println("Can not read from '"+usbCfgFile.getAbsolutePath()+"'. I will try to run 'usb-devices' now!");
//			getConfigExec();
//		}
//		finally{
//			if(br!=null){
//				br.close();
//			}
//			if(fr!=null){
//				fr.close();
//			}
//		}
//	}
}