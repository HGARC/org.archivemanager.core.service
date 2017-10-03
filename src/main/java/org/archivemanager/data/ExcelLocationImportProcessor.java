package org.archivemanager.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityQuery;
import org.heed.openapps.entity.EntityResultSet;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.data.FileImportProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 <Row ss:AutoFitHeight="0" ss:Height="50.25" ss:StyleID="s79">
    <Cell ss:StyleID="s67"><Data ss:Type="String">Building</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s68"><Data ss:Type="String">True Building</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s69"><Data ss:Type="String">Floor</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s82"><Data ss:Type="String">Aisle</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s69"><Data ss:Type="String">Bay</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s69"><Data ss:Type="String">REAL Bay</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s69"><Data ss:Type="String">Shelf Location</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">Original Building</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">Collection</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s67"><Data ss:Type="String">Accession #</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s80"><Data ss:Type="String">Dates</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">Notes</Data><NamedCell ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s90"><Data ss:Type="String">Boxes</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s88"><Data ss:Type="String">new box</Data></Cell>
   </Row>
 *
 */
public class ExcelLocationImportProcessor extends FileImportProcessor {
	private static final long serialVersionUID = 5333827099578290787L;
	private EntityService entityService;
	private List<String> columns = new ArrayList<String>();
	
	
	public ExcelLocationImportProcessor(){}
	
	public void process(InputStream stream, Map<String, Object> properties) throws Exception {
		columns.clear();
		getEntities().clear();
		//XMLUtility.SAXParse(false, stream, new ImportHandler());
		POIFSFileSystem fs = new POIFSFileSystem(stream);
		HSSFWorkbook workbook = new HSSFWorkbook(fs);
		HSSFSheet sheet = workbook.getSheetAt(0);
		for(Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			Row data = rit.next();
			Entity row = new Entity(RepositoryModel.LOCATION);
			row.setUuid(UUID.randomUUID().toString());
			String building = clean(getStringCellValue(data.getCell(1)));
			String floor = clean(getStringCellValue(data.getCell(2)));
			String aisle = clean(getStringCellValue(data.getCell(3)));
			String bay = clean(getStringCellValue(data.getCell(5)));
			String collection = clean(getStringCellValue(data.getCell(8)));
			String number = clean(getStringCellValue(data.getCell(9)));
			String dates = clean(getStringCellValue(data.getCell(10)));
			String container = clean(getStringCellValue(data.getCell(12)));
			String code = clean(getStringCellValue(data.getCell(7)));
			
			row.addProperty(RepositoryModel.BUILDING, building);
			row.addProperty(RepositoryModel.FLOOR, floor);
			row.addProperty(RepositoryModel.AISLE, aisle);
			row.addProperty(RepositoryModel.BAY, bay);
			row.addProperty(RepositoryModel.COLLECTION, collection);
			row.addProperty(RepositoryModel.ACCESSION, number);
			row.addProperty(SystemModel.DESCRIPTION, dates);
			row.addProperty(RepositoryModel.CONTAINER, container);
			row.addProperty(RepositoryModel.CODE, code);
			getEntities().put(String.valueOf(row.getUuid()), row);
	    }		
	}
	protected String getStringCellValue(Cell cell) {
		if(cell == null) return "";
		switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getRichStringCellValue().getString();
        case Cell.CELL_TYPE_NUMERIC:
            if(DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toString();
            } else {
                return String.valueOf((int)cell.getNumericCellValue());
            }
        case Cell.CELL_TYPE_BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        default:
            return "";
		}
	}
	/*
	public void process(InputStream stream) throws Exception {
		columns.clear();
		getEntities().clear();
		//XMLUtility.SAXParse(false, stream, new ImportHandler());
		String in = convertStreamToString(stream);
		Document doc = Jsoup.parse(in, "UTF-8");
		Elements elements = doc.select("ROW");
		for(int i=0; i < elements.size(); i++) {
			Element el1 = elements.get(i);
			Elements data = el1.children();
			Entity row = new Entity(RepositoryModel.LOCATION);
			row.setUid(UUID.randomUUID().toString());
			String building = clean(data.get(1).text());
			String floor = clean(data.get(2).text());
			String aisle = clean(data.get(3).text());
			String bay = clean(data.get(5).text());
			String collection = clean(data.get(8).text());
			String number = clean(data.get(9).text());
			String dates = clean(data.get(10).text());
			String container = clean(data.get(12).text());
			String code = clean(data.get(7).text());
			
			row.addProperty(RepositoryModel.BUILDING, building);
			row.addProperty(RepositoryModel.FLOOR, floor);
			row.addProperty(RepositoryModel.AISLE, aisle);
			row.addProperty(RepositoryModel.BAY, bay);
			row.addProperty(RepositoryModel.COLLECTION, collection);
			row.addProperty(RepositoryModel.ACCESSION, number);
			row.addProperty(RepositoryModel.DESCRIPTION, dates);
			row.addProperty(RepositoryModel.CONTAINER, container);
			
			getEntities().put(String.valueOf(row.getUid()), row);
			
		}
	}
	*/
	protected String clean(String in) {
		return in.trim();
	}
	
	public class ImportHandler extends DefaultHandler {
		Entity row = null;
		boolean inData = false;
		boolean tossRow = false;
		StringBuffer data = null;
		int currField = 0;
		String collectionTitle = "";
		
		public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException	{
			if(qName.equals("FIELD")) {
				columns.add(attrs.getValue("NAME"));
			} else if(qName.equals("ROW")) {
				row = new Entity(RepositoryModel.LOCATION);
				row.setUuid(UUID.randomUUID().toString());
				currField = 0;
			} else if(qName.equals("DATA")) {
				data = new StringBuffer();
				inData = true;
			}
		}
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(inData) {
				data.append(ch,start,length);
			}
		}
		public void endElement(String namespaceURI, String sName, String qName) throws SAXException	{
			if(qName.equals("ROW")) {
				getEntities().put(String.valueOf(row.getUuid()), row);
				//else System.out.println("Tossed duplicate row");
				//tossRow = false;
			} else if(qName.equals("COL")) {
				try {
					if(currField == 0) {
						
					} else if(currField == 1) {
						/*
						String donorTitle = data.toString().equals("same") ? collectionTitle : data.toString();
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Donor");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, donorTitle);
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
						*/
					} else if(currField == 2) {
						/*
						String[] date = data.toString().split("/");
						String month = date[0].length() == 1 ? "0"+date[0] : date[0];
						String day = date[1].length() == 1 ? "0"+date[1] : date[1];
						if(date.length == 3) row.addProperty(RepositoryModel.ACCESSION_DATE, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
						*/
					} else if(currField == 3) {
						/*
						String[] ext = data.toString().split(" ");
						Entity extent = new Entity(RepositoryModel.EXTENT);
						String type = translateExtent(ext[1].trim());
						extent.addProperty(RepositoryModel.EXTENT_TYPE, type);
						extent.addProperty(RepositoryModel.EXTENT_VALUE, ext[0]);
						Association assoc = new Association(RepositoryModel.EXTENTS, 0, 0);
						assoc.setTargetEntity(extent);
						row.getSourceAssociations().add(assoc);
						row.addProperty(RepositoryModel.EXTENT_TYPE, type);
						row.addProperty(RepositoryModel.EXTENT_VALUE, ext[0]);
						*/
					} else if(currField == 4) {
						//row.addProperty(RepositoryModel.PAGEBOX_QUANTITY, data.toString().trim());
					} else if(currField == 5) {
						
					} else if(currField == 6) {
						
					} else if(currField == 7) {
						collectionTitle = data.toString();
						EntityQuery query = new EntityQuery(RepositoryModel.COLLECTION, "name", data.toString(), null, true);
						EntityResultSet result = entityService.search(query);
						if(result.getResults().size() > 0) {
							Entity collection = result.getResults().get(0);
							collection.addProperty(SystemModel.TITLE, data.toString());
							Association assoc = new Association(RepositoryModel.LOCATIONS, collection.getId(), 0);
							assoc.setSourceEntity(collection);
							row.getTargetAssociations().add(assoc);
						} else {
							Entity collection = new Entity(RepositoryModel.COLLECTION);
							collection.addProperty(SystemModel.TITLE, data.toString());
							collection.addProperty(SystemModel.NAME, data.toString());
							Association assoc = new Association(RepositoryModel.LOCATIONS, 0, 0);
							assoc.setSourceEntity(collection);
							row.getTargetAssociations().add(assoc);
						}
					} else if(currField == 8) {
						
					} else if(currField == 9) {
						
					} else if(currField == 10) {
						
					} else if(currField == 11) {
						//row.addProperty(RepositoryModel.ACCESSION_COST, data.toString());
					} else if(currField == 12) {
					} else if(currField == 13) {
					} else if(currField == 14 && data.length() > 0) {
						/*
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Address");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
						*/
					} else if(currField == 15) {
					} else if(currField == 16) {
					} else if(currField == 17) {
					} else if(currField == 18) {
						/*
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Access Restriction");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
						*/
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				currField++;
				inData = false;
			}
		}
	}
	
	protected String translateExtent(String in) {
		if(in.equals("env.") || in.equals("envolope") || in.equals("env")) return "envelope";
		return in;
	}
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
}
