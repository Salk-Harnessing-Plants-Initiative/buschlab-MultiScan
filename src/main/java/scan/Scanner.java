package scan;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner{
	final private Logger log=Logger.getLogger(this.getClass().getName());
	private int bus=-1;
	private int port=-1;
	private int dev=-1;
	private int logical=-1;
	
	private String driver;
	private int resolution;
	private Rectangle[] scanRanges;
	private String format;
	private String[] alternativeOptionString;
	
	private Process process;
	String command;
	private double progress;
	
	private PropertyChangeSupport processing;
	private PropertyChangeSupport progressChange;
	
	public Scanner(int bus,int port,int dev,String driver,int resolution,Rectangle range1,Rectangle range2,String format){
		this.bus=bus;
		this.port=port;
		this.dev=dev;
		this.driver=driver;
		
		this.resolution=resolution; //1200;
		this.scanRanges=new Rectangle[2];
		this.scanRanges[0]=range1; //new Rectangle(150,40,140,140);
		this.scanRanges[1]=range2; //new Rectangle(5,40,140,140);
		this.format=format; //"tiff";
		
		processing=new PropertyChangeSupport(this);
		progressChange=new PropertyChangeSupport(this);
	}
	
	public Scanner(int bus,int port,int dev,String driver,String[] optionString){
		this.bus=bus;
		this.port=port;
		this.dev=dev;
		this.driver=driver;
		
		this.alternativeOptionString=optionString;
		
		processing=new PropertyChangeSupport(this);
		progressChange=new PropertyChangeSupport(this);
	}
	
	public String getHash(){
		return bus+"."+port;
	}
	
	public void setLogical(int logical){
		this.logical=logical;
	}
	
	public int getLogical(){
		return logical;
	}
	
	public void fire(){
		processing.firePropertyChange("test",1,2);
	}
	
	public int scanImage(int row,String dstFilename){
		File dstFile=new File(dstFilename);
//		command=String.format("scanimage -p -d epkowa:interpreter:%03d:%03d -t %d -l %d -x %d -y %d --x-resolution %d --y-resolution %d --format=%s > %s",bus,dev,sr.x,sr.y,sr.width,sr.height,resolution,resolution,format,dstFilename);
		String[] args=null;
		if(alternativeOptionString!=null){
			String[] optparts=alternativeOptionString[row].split("\\s+");
			args=new String[optparts.length+4];
			args[0]="/usr/bin/scanimage";
			args[1]="-p";
			args[2]="-d";
			args[3]=String.format(driver+":%03d:%03d",bus,dev);
			for(int i=0;i<optparts.length;++i){
				args[4+i]=optparts[i];
			};
		}
		else{
			Rectangle sr=scanRanges[row];
			args=new String[]{"/usr/bin/scanimage",
							  "-p",
							  "-d",String.format(driver+":%03d:%03d",bus,dev),
							  "-t",String.format("%d",sr.x),"-l",String.format("%d",sr.y),"-x",String.format("%d",sr.width),"-y",String.format("%d",sr.height),
							  String.format("--x-resolution=%d",resolution),String.format("--y-resolution=%d",resolution),
							  String.format("--format=%s",format)
							 };
		}
//		for(int i=0;i<args.length;++i){
//			System.out.println(args[i]);
//		}
		//System.out.println(command);
		ProcessBuilder pb=new ProcessBuilder(args);
		pb.redirectOutput(dstFile);
		
		String s=null;
		List<String> lastStdOut = new ArrayList<String>();
		List<String> lastStdErr = new ArrayList<String>();
		process=null;
		processing.firePropertyChange("ScanProcessing",-1,logical);
		//log.info("Scanner "+logical+": scan started.");
		int lastReturnValue = 999;
		try {
			process=pb.start(); //Runtime.getRuntime().exec(command);
//			process=Runtime.getRuntime().exec("cat /home/GMI/christian.goeschl/progress.txt");
//			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// read the output from the command
			while ((s = stdError.readLine()) != null) {
				if(s.contains("Progress")){
					Pattern doublePattern=Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
					Matcher m=doublePattern.matcher(s);
					double curProgress=progress;
					if(m.find()){
						curProgress=Double.parseDouble(m.group());
					}
					//double curProgress=strScan.nextDouble();
					//if(curProgress-progress>5){
						progressChange.firePropertyChange("ProgressChanged",logical,curProgress);
					//}
					progress=curProgress;
				}
				else{
					lastStdErr.add(s);
				}
			}
			
//			try {
				lastReturnValue =process.waitFor();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			log.info("scanner "+logical+" interrupted.");
			lastReturnValue =0;
		}
		finally{
			//lastReturnValue=process.exitValue();
			//System.out.println("scanimage exit value: "+lastReturnValue);
			if(lastReturnValue !=0){
				String completeErr="";
				for(String errStr: lastStdErr){
					completeErr+=errStr+"\n";
				}
				log.warning(completeErr);
			}
			processing.firePropertyChange("ScanFinished",-1,logical);
		}
		return lastReturnValue;
	}
	
	public void addProcessingListener(PropertyChangeListener l){
		processing.addPropertyChangeListener(l);
	}
	
	public void removeProcessingListener(PropertyChangeListener l){
		processing.removePropertyChangeListener(l);
	}

	public void addProgressListener(PropertyChangeListener l){
		progressChange.addPropertyChangeListener(l);
	}
	
	public void removeProgressListener(PropertyChangeListener l){
		progressChange.removePropertyChangeListener(l);
	}

	public void destroyProcess(){
		if(process!=null)
			process.destroy();
	}
	
	public boolean processing(){
		return process!=null;
	}
}
