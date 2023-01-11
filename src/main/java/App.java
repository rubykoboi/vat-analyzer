import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class App {

	private static String printLog = "";
	/** SHARED PATHS
		final static String LOG_FILE = "S:\\Purchasing\\GeneralShare\\Robbi Programs\\LOG_FILES\\VAT_ANALYZER Log File.txt";
	 */
//	/** LOCAL PATHS
		final static String LOG_FILE = "C:\\Vat Analyzer\\VAT_ANALYZER Log File.txt";
//	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new App();
					BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE));
					bw.write(printLog);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	public void out(String stringToPrint) {
		System.out.println(stringToPrint);
		printLog += stringToPrint+"\n";
	}

}
