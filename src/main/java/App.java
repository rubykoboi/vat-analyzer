import java.awt.EventQueue;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class App {

	private static String printLog = "";
	/** SHARED PATHS
		final static String LOG_FILE = "S:\\Purchasing\\GeneralShare\\Robbi Programs\\LOG_FILES\\VAT_ANALYZER Log File.txt";
	 */
//	/** LOCAL PATHS
		final static String LOG_FILE = "C:\\Vat Analyzer\\VAT_ANALYZER Log File.txt";
//	 */
	final static String DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop\\";
	final static String INVOICE_REGEX = "Invoice Number : ([A-Z]{2}\\d{7})";
	final static String VAT_REGEX = "VAT : (.*)";
	final static Pattern INVOICE_PATTERN = Pattern.compile(INVOICE_REGEX);
	final static Pattern VAT_PATTERN = Pattern.compile(VAT_REGEX);
	static Matcher invoiceMatcher, vatMatcher;
	final static String[] REGION_NAMES = {"Invoice","Ship From","Ship To","VAT","Total Amount",
				"VAT %","VAT Amount","Total Incl"};
	final static Rectangle2D[] REGION_RECT = {
			new Rectangle2D.Double(400, 80, 200, 10), // 0 Invoice
			new Rectangle2D.Double(150, 100, 200, 20), // 1 Ship From Country
			new Rectangle2D.Double(150, 180, 200, 10), // 2 Ship To Country
			new Rectangle2D.Double(150, 210, 200, 10), // 3 VAT from Ship To Country
			new Rectangle2D.Double(500, 650, 200, 7), // 4 Total Amount
			new Rectangle2D.Double(500, 660, 200, 7), // 5 Vat %
			new Rectangle2D.Double(500, 669, 200, 17), // 6 VAT Amount
			new Rectangle2D.Double(500, 686, 200, 7)  // 7 Total Including VAT
	};
	
	String[] values = new String[REGION_NAMES.length];
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
		String pdfFile = "";
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.getLookAndFeelDefaults().put("defaultFont",  new Font("Arial", Font.BOLD, 14));
		} catch (Throwable ex) {
			ex.printStackTrace();
			out(ex.toString());
		}
		
		// SELECT PDF FILE
//		JFileChooser fileChooser = new JFileChooser(DESKTOP_PATH);
//		fileChooser.setMultiSelectionEnabled(false);
//		int response = fileChooser.showOpenDialog(null);
//		if(response == JFileChooser.APPROVE_OPTION) {
//			pdfFile = fileChooser.getSelectedFile().getAbsolutePath();
//			out("We have chosen a file!!!");
//			out(pdfFile);
//		}
		
		pdfFile = "C:\\Users\\safavieh\\Desktop\\EU.pdf";
		// READ IN PDF FILE
		PDDocument doc = PDDocument.load((new File(pdfFile)));
		int pageCount = doc.getNumberOfPages();
		out("There are " + pageCount + " pages in the selected pdf file.");
		PDPage page = new PDPage();
		
		// GO THROUGH EACH PAGE
		for(int pageNum = 1; pageNum <= 2; pageNum+=2) {
			page = doc.getPage(pageNum-1);
		
			// STRIP PDF FILE ONTO PARTS NEEDED
			PDFTextStripperByArea pdfsa = new PDFTextStripperByArea();
			pdfsa.setSortByPosition(true);
			
			for(int region = 0; region < REGION_NAMES.length; region++) {
				pdfsa.addRegion(REGION_NAMES[region], REGION_RECT[region]);
				pdfsa.extractRegions(page);
				if(REGION_NAMES[region] == "Invoice") {
					invoiceMatcher = INVOICE_PATTERN.matcher(pdfsa.getTextForRegion(REGION_NAMES[region]).trim());
					if(invoiceMatcher.find()) values[region] = invoiceMatcher.group(1);
					else values[region] = "NO INVOICE FOUND";
				} else if(REGION_NAMES[region] == "VAT") {
					vatMatcher = VAT_PATTERN.matcher(pdfsa.getTextForRegion(REGION_NAMES[region]).trim());
					if(vatMatcher.find()) values[region] = vatMatcher.group(1);
					else values[region] = "NO VAT FOUND";
				} else {
					values[region] = pdfsa.getTextForRegion(REGION_NAMES[region]).trim();	
				}
				out(REGION_NAMES[region] + " : [" + values[region] + "]");
			}
		}
		
	}
	
	public void out(String stringToPrint) {
		System.out.println(stringToPrint);
		printLog += stringToPrint+"\n";
	}

}
