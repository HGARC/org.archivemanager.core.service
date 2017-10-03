package org.archivemanager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.heed.openapps.util.NumberUtility;

import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;


public class PartisanReviewIndexParser {
	private Map<String,Long> lookup = new HashMap<String,Long>();
	private RomanNumeralConverter converter = new RomanNumeralConverter();
	
	public void run() {
		try {
			/*
			 PdfReader reader = new PdfReader("/opt/programming/ArchiveManager/misc/PR Index book.pdf");
		     FileOutputStream fos = new FileOutputStream("/opt/programming/ArchiveManager/misc/PR Index book.txt");
		     int n = reader.getNumberOfPages();
		     PRTextExtractionStrategy strategy = new PRTextExtractionStrategy();
		     for(int page = 1; page <= n; page++) {
		    	 fos.write(PdfTextExtractor.getTextFromPage(reader, page, strategy).getBytes("UTF-8"));
		     }
		     fos.flush();
		     fos.close();
		     */
			
			
			CsvReader products = new CsvReader("/opt/programming/ArchiveManager/data/export_283885.csv");			
			products.readHeaders();
			while (products.readRecord()) {
				String id = products.get(0);
				String type = products.get(1);
				String volume = products.get(2);
				String issue = products.get(3);
				if(!type.equals("collection") && !type.equals("category")) {
					String num = volume.substring(5);
					volume = converter.toRomanNumerals(Integer.valueOf(num));
					issue = issue.replace("No.", "").trim();
					lookup.put(volume + "," + issue, Long.valueOf(id));
					System.out.println("lookup value ("+id+"): "+volume + "," + issue);
				}				
			}
			products.close();
			System.out.println(lookup.size() + " items added to lookup");
			
			int errors = 0;
						
			FileWriter writer = new FileWriter("/opt/programming/ArchiveManager/data/partisan_review/ArticlesAndComments.html");
		    FileReader reader = new FileReader("/opt/programming/ArchiveManager/data/partisan_review/ArticlesAndComments.txt");
		    errors = processFile(reader, writer);
		    System.out.println("Articles And Comments "+errors);
		    		    
			FileWriter writer2 = new FileWriter("/opt/programming/ArchiveManager/data/partisan_review/BookReviewers.html");
		    FileReader reader2 = new FileReader("/opt/programming/ArchiveManager/data/partisan_review/BookReviewers.txt");
		    errors = processFile(reader2, writer2);
		    System.out.println("Book Reviewers "+errors);
		    
		    FileWriter writer3 = new FileWriter("/opt/programming/ArchiveManager/data/partisan_review/BookReviews.html");
		    FileReader reader3 = new FileReader("/opt/programming/ArchiveManager/data/partisan_review/BookReviews.txt");
		    errors = processFile(reader3, writer3);
		    System.out.println("Book Reviews "+errors);
		    
		    FileWriter writer4 = new FileWriter("/opt/programming/ArchiveManager/data/partisan_review/Fiction.html");
		    FileReader reader4 = new FileReader("/opt/programming/ArchiveManager/data/partisan_review/Fiction.txt");
		    errors = processFile(reader4, writer4);
		    System.out.println("Fiction "+errors);
		    
		    FileWriter writer5 = new FileWriter("/opt/programming/ArchiveManager/data/partisan_review/Poetry.html");
		    FileReader reader5 = new FileReader("/opt/programming/ArchiveManager/data/partisan_review/Poetry.txt");
		    errors = processFile(reader5, writer5);
		    System.out.println("Poetry "+errors);
		    
		} catch(IOException e) {
			e.printStackTrace();
		} 
	}
	protected int processFile(FileReader textReader, FileWriter writer) throws IOException {
		int errors = 0;
		writer.write("<style>.author{font-weight:bold;padding-top:5px;}.error{color:red;}</style>");
		String state = "author";
		BufferedReader br = new BufferedReader(textReader);                                                 
	    String data = br.readLine();
	    while(data != null) {
	    	if(data.length() <= 1) {
	    		state = "author";
	    	} else if(state.equals("author")) {
	    		writer.write("<div class=\"author\">"+data+"</div>");
	    		state = "publication";
	    	} else if(state.equals("publication")) {
	    		//http://sweetchuck:8080/exhibitions/partisan-review/search/detail?id=283894
	    		int start = data.lastIndexOf("Vol.");
	    		int end = data.indexOf(",", start);
	    		int iss = data.indexOf(",", end+1);
	    		if(start > -1 && end > -1 && iss > -1) {
		    		String volume = data.substring(start+4, end).trim().toUpperCase();
		    		String issue = data.substring(end+1, iss).replace("No.", "").trim();
		    		if(NumberUtility.isInteger(volume))
		    			volume = converter.toRomanNumerals(Integer.valueOf(volume));
		    		if(volume.contains("NO.")) {
		    			String[] parts = volume.split("NO.");
		    			volume = parts[0].replace(".", "").trim();
		    			issue = parts[1].trim();
		    		}
		    		if(issue.contains("-")) {
		    			issue = "Nos. "+issue;
		    		}
		    		Long id = lookup.get(volume+","+issue);
		    		if(id == null) {
		    			//writer.write("<div class=\"error\">no matching id : " + data + "</div>");
		    			errors++;
		    		} else
		    			writer.write("<div class=\"publication\"><a href=\"/exhibitions/partisan-review/search/detail?id="+id+"\">"+data+"</a></div>");
	    		} else {
	    			//writer.write("<div class=\"error\">problem parsing : " + data + "</div>");
	    			errors++;
	    		}
	    	}
	    	data = br.readLine();
	    }
	    br.close();
	    writer.close();
	    return errors;
	}
	public static void main(String[] args) {
		PartisanReviewIndexParser parser = new PartisanReviewIndexParser();
		parser.run();
	}
	public class PRTextExtractionStrategy extends SimpleTextExtractionStrategy {
		@Override
		public void renderText(TextRenderInfo arg0) {
			// TODO Auto-generated method stub
			super.renderText(arg0);
		}
	}
}
