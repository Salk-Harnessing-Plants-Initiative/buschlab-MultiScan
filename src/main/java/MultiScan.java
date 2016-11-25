import gui.MultiScanGUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class MultiScan {

	public static void main(String[] args){
//		File cfgFile=null;
		InputStream cfgStream = null;
		if(args.length==0) {
				cfgStream = MultiScan.class.getClassLoader().getResourceAsStream("multiscan.cfg");
		}
		else {
			try {
				cfgStream = new FileInputStream(args[0]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
//			cfgFile = new File(args[0]);
		}
		MultiScanGUI gui=new MultiScanGUI(cfgStream);
	}
}
