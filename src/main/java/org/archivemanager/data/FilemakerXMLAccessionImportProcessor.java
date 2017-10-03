package org.archivemanager.data;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityQuery;
import org.heed.openapps.entity.EntityResultSet;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.data.FileImportProcessor;
import org.heed.openapps.util.DecodingUtility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <DATABASE DATEFORMAT="M/d/yyyy" LAYOUT="" NAME="Accession 2012.fp7" RECORDS="21849" TIMEFORMAT="h:mm:ss a"/>
 * <METADATA>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Access. #:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Appraisal:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Birth:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Box #'s:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Boxes in Collection:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Boxes in Shipment:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Category:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Collectee:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="date entered" TYPE="DATE"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Date:" TYPE="DATE"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Death:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Description:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="global" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Inventory" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Listed:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Location:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="mark" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Nationality:" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="paige" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="pkg" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="print check" TYPE="TEXT"/>
 * <FIELD EMPTYOK="YES" MAXREPEAT="1" NAME="Restr:" TYPE="TEXT"/>
 * </METADATA>
 *
 */
public class FilemakerXMLAccessionImportProcessor extends FileImportProcessor {
	private static final long serialVersionUID = -6301629044945405760L;
	private EntityService entityService;
	private List<String> columns = new ArrayList<String>();
	
	
	public FilemakerXMLAccessionImportProcessor() {
		super();
	}
	public FilemakerXMLAccessionImportProcessor(String name) {
		super(RepositoryModel.ACCESSION.toString(), name);
		//this.searchService = searchService;
	}	
	
	public void process(InputStream stream, Map<String, Object> properties) throws Exception {
		columns.clear();
		getEntities().clear();
		//XMLUtility.SAXParse(false, stream, new ImportHandler());
		String in = DecodingUtility.convertStreamToString(stream);
		Document doc = Jsoup.parse(in, "UTF-8");
		Elements elements = doc.select("ROW");
		for(int i=0; i < elements.size(); i++) {
			Element el1 = elements.get(i);
			Elements data = el1.children();
			Entity row = new Entity(RepositoryModel.ACCESSION);
			row.setUuid(UUID.randomUUID().toString());
			String date = clean(data.get(9).text());
			String number = clean(data.get(0).text());
			String collection = clean(data.get(7).text());
			String general_note = clean(data.get(11).text());
			row.addProperty(SystemModel.NAME, collection+" ("+date+")");
			row.addProperty(RepositoryModel.ACCESSION_NUMBER, number);			
			row.addProperty(RepositoryModel.ACCESSION_DATE, date);
			row.addProperty(RepositoryModel.ACCESSION_GENERAL_NOTE, general_note);
			getCollection(row, collection);
			getEntities().put(String.valueOf(row.getUuid()), row);
			
		}
	}
	protected String clean(String in) {
		return in.trim();
	}
	protected void getCollection(Entity entity, String collectionName) throws Exception {
		EntityQuery query = new EntityQuery(RepositoryModel.COLLECTION, "name", collectionName, null, true);
		EntityResultSet result = entityService.search(query);
		if(result.getResults().size() > 0) {
			Entity collection = result.getResults().get(0);
			collection.addProperty(SystemModel.TITLE, collectionName);
			Association assoc = new Association(RepositoryModel.ACCESSIONS, collection.getId(), 0);
			assoc.setSourceEntity(collection);
			entity.getTargetAssociations().add(assoc);
		} else {
			Entity collection = new Entity(RepositoryModel.COLLECTION);
			collection.addProperty(SystemModel.TITLE, collectionName);
			collection.addProperty(SystemModel.NAME, collectionName);
			Association assoc = new Association(RepositoryModel.ACCESSIONS, 0, 0);
			assoc.setSourceEntity(collection);
			entity.getTargetAssociations().add(assoc);
		}
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
				row = new Entity(RepositoryModel.ACCESSION);
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
						collectionTitle = data.toString();
						EntityQuery query = new EntityQuery(RepositoryModel.COLLECTION, "name", data.toString(), null, true);
						EntityResultSet result = entityService.search(query);
						if(result.getResults().size() > 0) {
							Entity collection = result.getResults().get(0);
							collection.addProperty(SystemModel.TITLE, data.toString());
							Association assoc = new Association(RepositoryModel.ACCESSIONS, collection.getId(), 0);
							assoc.setSourceEntity(collection);
							row.getTargetAssociations().add(assoc);
						} else {
							Entity collection = new Entity(RepositoryModel.COLLECTION);
							collection.addProperty(SystemModel.TITLE, data.toString());
							collection.addProperty(SystemModel.NAME, data.toString());
							Association assoc = new Association(RepositoryModel.ACCESSIONS, 0, 0);
							assoc.setSourceEntity(collection);
							row.getTargetAssociations().add(assoc);
						}
					} else if(currField == 1) {
						String donorTitle = data.toString().equals("same") ? collectionTitle : data.toString();
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Donor");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, donorTitle);
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
					} else if(currField == 2) {
						String[] date = data.toString().split("/");
						String month = date[0].length() == 1 ? "0"+date[0] : date[0];
						String day = date[1].length() == 1 ? "0"+date[1] : date[1];
						if(date.length == 3) row.addProperty(RepositoryModel.ACCESSION_DATE, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
					} else if(currField == 3) {
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
						
					} else if(currField == 4) {
						row.addProperty(RepositoryModel.PAGEBOX_QUANTITY, data.toString().trim());
					} else if(currField == 5) {
						
					} else if(currField == 6) {
						
					} else if(currField == 7) {
						row.addProperty(RepositoryModel.ACCESSION_GENERAL_NOTE, data.toString());
					} else if(currField == 8) {
						
					} else if(currField == 9) {
						
					} else if(currField == 10) {
						
					} else if(currField == 11) {
						row.addProperty(RepositoryModel.ACCESSION_COST, data.toString());
					} else if(currField == 12) {
					} else if(currField == 13) {
					} else if(currField == 14 && data.length() > 0) {
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Address");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
					} else if(currField == 15) {
					} else if(currField == 16) {
					} else if(currField == 17) {
					} else if(currField == 18) {
						Entity restrictions = new Entity(SystemModel.NOTE);
						restrictions.addProperty(SystemModel.NOTE_TYPE, "Access Restriction");
						restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
						Association assoc = new Association(SystemModel.NOTES, 0, 0);
						assoc.setTargetEntity(restrictions);
						row.getSourceAssociations().add(assoc);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				currField++;
				inData = false;
			}
		}
	}
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	protected String translateExtent(String in) {
		if(in.equals("env.") || in.equals("envolope") || in.equals("env")) return "envelope";
		return in;
	}
}
