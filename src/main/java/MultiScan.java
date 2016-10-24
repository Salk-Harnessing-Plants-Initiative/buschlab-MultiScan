import gui.MultiScanGUI;

public class MultiScan {

	public static void main(String[] args){
		String cfgPath="/opt/multiscan"+System.getProperty("file.separator")+"multiscan.cfg";
		if(args.length!=0){
			cfgPath=args[0];
		}
		MultiScanGUI gui=new MultiScanGUI(cfgPath);
	}
}
