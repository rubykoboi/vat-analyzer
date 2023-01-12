import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

public class App {

	private static String printLog = "";
	/** SHARED PATHS
		final static String LOG_FILE = "S:\\Purchasing\\GeneralShare\\Robbi Programs\\LOG_FILES\\VAT_ANALYZER Log File.txt";
	 */
//	/** LOCAL PATHS
		final static String LOG_FILE = "C:\\Vat Analyzer\\VAT_ANALYZER Log File.txt";
//	 */
	final static String DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop\\";
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new App();
					BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE));
					bw.write(printLog);
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public App() throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.getLookAndFeelDefaults().put("defaultFont",  new Font("Arial", Font.BOLD, 14));
		} catch (Throwable ex) {
			ex.printStackTrace();
			out(ex.toString());
		}
		// SELECT PDF FILE
		JFileChooser fileChooser = new JFileChooser(DESKTOP_PATH);
		fileChooser.setMultiSelectionEnabled(false);
		int response = fileChooser.showOpenDialog(null);
		if(response == JFileChooser.APPROVE_OPTION) {
			out("We have chosen a file!!!");
			out(fileChooser.getSelectedFile().getAbsolutePath());
		}
		// READ IN PDF FILE
	}
	
	public void out(String stringToPrint) {
		System.out.println(stringToPrint);
		printLog += stringToPrint+"\n";
	}

}
