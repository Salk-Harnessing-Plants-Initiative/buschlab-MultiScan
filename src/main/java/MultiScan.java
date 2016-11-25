import gui.MultiScanGUI;

import java.io.File;
import java.net.URISyntaxException;

public class MultiScan {

	public static void main(String[] args){
		File cfgFile=null;
		if(args.length==0) {
			try {
				cfgFile = new File(MultiScan.class.getClassLoader().getResource("multiscan.cfg").toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else {
			cfgFile = new File(args[0]);
		}
		MultiScanGUI gui=new MultiScanGUI(cfgFile);
	}
}
