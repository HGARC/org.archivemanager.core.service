package org.archivemanager.data;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.heed.openapps.SystemModel;
import org.heed.openapps.dictionary.ClassificationModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityQuery;
import org.heed.openapps.entity.EntityResultSet;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.data.FileImportProcessor;

/*
<Row ss:Height="14.0616">
<Cell ss:StyleID="Excel_20_Built-in_20_Normal"><Data ss:Type="String">Location</Data></Cell>
<Cell ss:StyleID="Excel_20_Built-in_20_Normal"><Data ss:Type="String">Name</Data></Cell>
<Cell ss:StyleID="Excel_20_Built-in_20_Normal"><Data ss:Type="String">Contents</Data></Cell>
<Cell ss:StyleID="Excel_20_Built-in_20_Normal"><Data ss:Type="String">Collection</Data></Cell>
<Cell ss:Index="1024"/>
</Row>
*/
public class ExcelClassificationEntryImportProcessor extends FileImportProcessor {
	private static final long serialVersionUID = 8393268537843399633L;
	private final static Logger log = Logger.getLogger(ExcelClassificationEntryImportProcessor.class.getName());
	private EntityService entityService;
	//private SearchService searchService;
	private List<String> columns = new ArrayList<String>();
	
	
	public ExcelClassificationEntryImportProcessor(){}
	public ExcelClassificationEntryImportProcessor(String name) {
		super(ClassificationModel.ENTRY.toString(), name);
	}	
	
	public void process(InputStream stream, Map<String, Object> properties) throws Exception {
		columns.clear();
		getEntities().clear();
		//XMLUtility.SAXParse(false, stream, new ImportHandler());
		//POIFSFileSystem fs = new POIFSFileSystem(stream);
		//HSSFWorkbook workbook = new HSSFWorkbook(fs);
		//HSSFSheet sheet = workbook.getSheetAt(0);
		Workbook wb = WorkbookFactory.create(stream);
		Sheet sheet = wb.getSheetAt(0);
		Map<String, Entity> names = new HashMap<String, Entity>();
		int count = 0;
		int missing = 0;
		for(Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			Row data = rit.next();
			if(count > 0) {
				Entity row = new Entity(ClassificationModel.ENTRY);
				row.setUuid(UUID.randomUUID().toString());
				String name = clean(getStringCellValue(data.getCell(0)));
				String contents = clean(getStringCellValue(data.getCell(2)));
				String collection = clean(getStringCellValue(data.getCell(3)));
				String location = clean(getStringCellValue(data.getCell(4)));
				String date = clean(getStringCellValue(data.getCell(5)));
				try {
					if(!names.containsKey(name)) {
						String queryStr = name;//.replace(",", "");
						EntityQuery query = new EntityQuery(ClassificationModel.PERSON, queryStr);
						query.getFields().add("name");
						EntityResultSet result = entityService.search(query);
						if(result.getResultSize() == 0) {
							query = new EntityQuery(ClassificationModel.CORPORATION, name);
							query.getFields().add("name");
							result = entityService.search(query);
						}
						if(result.getResultSize() > 0) {
							names.put(name, result.getResults().get(0));
							log.info("found "+queryStr);
						} else {
							names.put(name, null);
							missing++;
							log.info("did not find "+queryStr);
						}
					}
					if(names.containsKey(name) && names.get(name) != null){
						Association assoc = new Association(ClassificationModel.ENTRIES);
						assoc.setSourceEntity(names.get(name));
						row.getTargetAssociations().add(assoc);
						//row.addProperty(SystemModel.NAME, name);
						row.addProperty(ClassificationModel.ITEMS, contents);
						row.addProperty(ClassificationModel.COLLECTION_NAME, collection);
						row.addProperty(SystemModel.NAME, name);
						if(location != null && location.length() > 0)
							row.addProperty(ClassificationModel.LOCATION, location);
						if(date != null && date.length() > 0) {
							String value = date.replace(" 00:00:00 EDT", "");
							row.addProperty(ClassificationModel.DATE, value);
						}
						getEntities().put(String.valueOf(row.getUuid()), row);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}				
			}
			count++;
	    }
		log.info("finished import missing "+missing+" of "+names.size());
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
	
	protected String clean(String in) {
		return in.trim();
	}
	
	protected String translateExtent(String in) {
		if(in.equals("env.") || in.equals("envolope") || in.equals("env")) return "envelope";
		return in;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

}
