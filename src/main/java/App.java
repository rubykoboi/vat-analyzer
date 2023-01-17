import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class App {

	private static String printLog = "";
	/** SHARED PATHS
		final static String LOG_FILE = "S:\\Purchasing\\GeneralShare\\Robbi Programs\\LOG FILES\\Vat Analyzer LOG_FILE.txt";
	 */
//	/** LOCAL PATHS
		final static String LOG_FILE = "C:\\Vat Analyzer\\VAT_ANALYZER Log File.txt";
//	 */
	final static String DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop\\";
	final static String INVOICE_REGEX = "Invoice Number : ([A-Z]{2}\\d{7})";
	final static String SHIP_FROM_REGEX = "([a-zA-Z ]+)$";
	final static String SHIP_TO_REGEX = "[\\d\\s\\.]*([a-zA-Z ]+)$(?!(\\nTel\\.))";
	final static String VAT_REGEX = "VAT :([\\w (N/A)]*)?";
	final static Pattern INVOICE_PATTERN = Pattern.compile(INVOICE_REGEX);
	final static Pattern SHIP_FROM_PATTERN = Pattern.compile(SHIP_FROM_REGEX);
	final static Pattern SHIP_TO_PATTERN = Pattern.compile(SHIP_TO_REGEX);
	final static Pattern VAT_PATTERN = Pattern.compile(VAT_REGEX);
	static Matcher invoiceMatcher, shipFromMatcher, shipToMatcher, vatMatcher;
	final static String[] REGION_NAMES = {"Invoice","Ship From","Ship To & VAT","Total Amount",
				"VAT %","VAT Amount","Total Incl"};
	final static String[] ADDITIONAL_COLUMNS = {"Ship From Country","Ship To Country",
			"VAT from Ship To Country","Total Amount","VAT %","VAT Amount","TOTAL INCLUDING VAT"};
	final static Rectangle2D[] REGION_RECT = {
			new Rectangle2D.Double(400, 80, 200, 10), // 0 Invoice
			new Rectangle2D.Double(150, 100, 200, 20), // 1 Ship From Country
			new Rectangle2D.Double(150, 183, 200, 50), // 2 Ship To Country & VAT
//			new Rectangle2D.Double(150, 210, 200, 10), // 3 VAT from Ship To Country
			new Rectangle2D.Double(500, 650, 200, 7), // 3 Total Amount
			new Rectangle2D.Double(500, 660, 200, 7), // 4 Vat %
			new Rectangle2D.Double(500, 669, 200, 17), // 5 VAT Amount
			new Rectangle2D.Double(500, 686, 200, 7)  // 6 Total Including VAT
	};
	final static int COLUMN_START = 16;
	String[] values = new String[REGION_NAMES.length+1];
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new App();
					BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE));
					bw.write(printLog);
					bw.close();
					System.exit(0); // Force exit, need to figure out what is causing it to not exit on its own (Hint: related to excel file chooser)
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public App() throws Exception {
		String pdfFile = "";
		String excelFile = "";
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.getLookAndFeelDefaults().put("defaultFont",  new Font("Arial", Font.BOLD, 14));
		} catch (Throwable ex) {
			ex.printStackTrace();
			out(ex.toString());
		}
		
		try {
			///** SELECT PDF FILE
			JFileChooser fileChooser1 = new JFileChooser(DESKTOP_PATH);
			fileChooser1.setDialogTitle("Choose PDF to extract");
			fileChooser1.setMultiSelectionEnabled(false);
			fileChooser1.setFileFilter(new FileNameExtensionFilter("pdf files (*.pdf)","pdf"));
			int response1 = fileChooser1.showOpenDialog(null);
			if(response1 == JFileChooser.APPROVE_OPTION) {
				pdfFile = fileChooser1.getSelectedFile().getAbsolutePath();
				out("We have chosen a file!!!");
				out(pdfFile);
			}
	
			// SELECT EXCEL FILE
			JFileChooser fileChooser2 = new JFileChooser(DESKTOP_PATH);
			fileChooser2.setDialogTitle("Choose Excel to write on");
			fileChooser2.setMultiSelectionEnabled(false);
			fileChooser2.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)","xlsx"));
			int response2 = fileChooser2.showOpenDialog(null);
			if(response2 == JFileChooser.APPROVE_OPTION) {
				excelFile = fileChooser2.getSelectedFile().getAbsolutePath();
				out("We have chosen an excel file!!!");
				out(excelFile);
			}
	
	
			//*/
	
	//		/** LOCAL HARD CODED PATHS
	//		pdfFile = "C:\\Users\\safavieh\\Downloads\\EU4.pdf";
	//		excelFile = "C:\\Users\\safavieh\\Downloads\\Sales Invoice Listing for EU October – November – December 2022.xlsx";
	//		 */		
	
			// READ EXCEL SHEET
			FileInputStream fis = new FileInputStream(excelFile);
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheetAt(0);
			XSSFFont bodyFont = wb.createFont();
			bodyFont.setFontName("Century Gothic");
			bodyFont.setFontHeight(8);
			XSSFCellStyle cellFont = wb.createCellStyle();
			cellFont.setFont(bodyFont);
			Row row;
			Cell cell;
			
			// CREATE ADDITIONAL HEADERS
			XSSFFont boldFont = wb.createFont();
			boldFont.setBold(true);
			boldFont.setFontName("Arial");
			boldFont.setFontHeight(9);
			XSSFCellStyle bold = wb.createCellStyle();
			bold.setFont(boldFont);
			row = sheet.getRow(6);
			for(int col = 0; col < 7; col++) {
				cell = row.createCell(col+16);
				cell.setCellValue(ADDITIONAL_COLUMNS[col]);
				cell.setCellStyle(bold);
			}
			/**	
				fis.close();
				FileOutputStream fos = new FileOutputStream(excelFile);
				wb.write(fos);
				wb.close();
				fos.close();
			*/
	
			int rowCount = sheet.getPhysicalNumberOfRows();
			
			/** 
			 * 	This section stores the Invoices in a HashMap  
			 * 	called invoicesMap, storing the index of its row
			 * 	as the value and the Item ID as the key
			 */
			HashMap<String,Integer> invoicesMap = new HashMap<String,Integer>();
			for(int rowNum = 7; rowNum < rowCount; rowNum++) {
				row = sheet.getRow(rowNum);
				cell = row.getCell(1);
				if(cell == null) continue;
				invoicesMap.put(cell.getStringCellValue(),rowNum);
			}
			
			// READ IN PDF FILE
			PDDocument doc = PDDocument.load((new File(pdfFile)));
			int pageCount = doc.getNumberOfPages();
			out("There are " + pageCount + " pages in the selected pdf file.");
			out(pdfFile);
			out("We are referencing the following excel sheet: \n\t"+excelFile);
			PDPage page = new PDPage();
			
			/**
			 * Go through each odd-numbered page in the pdf and extract
			 * After extracting, we find the row number that corresponds to the
			 * order on the pdf which info we extracted.
			 * After finding that row, we add the new information on the new columns
			 * that were added to the right.
			 */
			for(int pageNum = 1; pageNum <= pageCount; pageNum+=2) {
				page = doc.getPage(pageNum-1);
			
				/**
				 * We start stripping the pdf by area to find the 7 values we need
				 * The first value is for referencing the correct row on the excel sheet
				 * The rest are for adding onto the excel sheet
				 */
				PDFTextStripperByArea pdfsa = new PDFTextStripperByArea();
				pdfsa.setSortByPosition(true);
				String currentRegion = "";
				for(int region = 0; region < REGION_NAMES.length; region++) {
					pdfsa.addRegion(REGION_NAMES[region], REGION_RECT[region]);
					pdfsa.extractRegions(page);
					currentRegion = pdfsa.getTextForRegion(REGION_NAMES[region]).trim();
					if(REGION_NAMES[region] == "Invoice") { // 0
						invoiceMatcher = INVOICE_PATTERN.matcher(currentRegion);
						if(invoiceMatcher.find()) values[region] = invoiceMatcher.group(1);
						else values[region] = "NO INVOICE FOUND";
					} else if(REGION_NAMES[region] == "Ship From") { // 1
						out("Ship from : " + currentRegion);
						shipFromMatcher = SHIP_FROM_PATTERN.matcher(currentRegion);
						if(shipFromMatcher.find()) values[region] = shipFromMatcher.group(1).trim();
					} else if(REGION_NAMES[region] == "Ship To & VAT") { // 2
						
						out("Ship To & VAT: " + currentRegion);
						shipToMatcher = SHIP_TO_PATTERN.matcher(currentRegion);
						if(shipToMatcher.find()) values[region] = shipToMatcher.group(1);
						else {
							int beginIndex = 0;
							int endIndex = 0;
							if(currentRegion.indexOf("Tel.") == -1) out("there is no Tel. captured");
							else {
								beginIndex = currentRegion.indexOf(".", currentRegion.indexOf("Tel."));
							}
							if(currentRegion.indexOf("VAT") == -1) {
								out("there is no VAT captured");
								endIndex = currentRegion.length();
							}
							else endIndex = currentRegion.indexOf("VAT") -1;
							shipToMatcher = SHIP_TO_PATTERN.matcher(currentRegion.substring(beginIndex, endIndex));
							if(shipToMatcher.find()) values[region] = shipToMatcher.group(1);
						}
						out("SHIP TO : " + values[region]);
						vatMatcher = VAT_PATTERN.matcher(currentRegion);
						if(vatMatcher.find()) values[region+1] = vatMatcher.group(1).trim();
						else values[region+1] = "VAT NOT DETECTED";
						out("VAT : " + values[region+1]);
					} else { // 3-6
						values[region+1] = currentRegion;	
					}
				}
				
				/**
				 * Search the excel sheet of the corresponding invoice number
				 * to add the matching information to the same row.
				 * Go to that row and add the values extracted from the pdf onto the new columns
				 */
				if(invoicesMap.containsKey(values[0])) {
					out("Found invoice ["+values[0]+"] on row "+invoicesMap.get(values[0]));
					// GET THE FIRST ROW CORRESPONDING TO THE INVOICE FOUND
					row = sheet.getRow(invoicesMap.get(values[0])); 
					for(int col = 0; col < ADDITIONAL_COLUMNS.length; col++) {
						cell = row.createCell(col+COLUMN_START);
						cell.setCellValue(values[col+1]);
						out(" : "+values[col+1]);
					}
					out("Added additional columns for the values above");
				} else {
					out("We did not find invoice ["+values[0]+"] on the excel sheet");
				}
				
			}
			out("after block");
	
			fis.close();
			FileOutputStream fos = new FileOutputStream(excelFile);
			wb.write(fos);
			wb.close();
			fos.close();
			doc.close();
			Desktop.getDesktop().open(new File(excelFile));
		} catch (Exception ex) {
			out("Exception caught in main process: \n"+ex.getMessage());
		}
	}
	
	public void out(String stringToPrint) {
		System.out.println(stringToPrint);
		printLog += stringToPrint+"\n";
	}

}
