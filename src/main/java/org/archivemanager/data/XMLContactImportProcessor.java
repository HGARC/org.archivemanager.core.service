package org.archivemanager.data;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.heed.openapps.dictionary.ContactModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.data.FileImportProcessor;
import org.heed.openapps.util.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLContactImportProcessor extends FileImportProcessor {
	private static final long serialVersionUID = 4304662582348219112L;
	private EntityService entityService;
	private List<String> columns = new ArrayList<String>();
	
	
	public XMLContactImportProcessor(){}
	public XMLContactImportProcessor(String name, EntityService entityService) {
		super(ContactModel.CONTACT.toString(), name);
		this.entityService = entityService;
	}	
	
	public void process(InputStream stream, Map<String, Object> properties) throws Exception {
		columns.clear();
		getEntities().clear();
		XMLUtility.SAXParse(false, stream, new ImportHandler());
	}
	
	public class ImportHandler extends DefaultHandler {
		Entity row = null;
		Entity address = null;
		boolean inData = false;
		boolean tossRow = false;
		StringBuffer data = null;
		int currField = 0;
			
		public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException	{
			if(qName.equals("tr")) {
				row = new Entity(ContactModel.INDIVIDUAL);
				address = new Entity(ContactModel.ADDRESS);
				address.setUuid(UUID.randomUUID().toString());
				row.setUuid(UUID.randomUUID().toString());
				currField = 0;
			} else if(qName.equals("td")) {
				data = new StringBuffer();
				inData = true;
			}
		}
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(inData) {
				String b = new String(ch,start,length).replace("\n", "");
				data.append(b);
			}
		}
		public void endElement(String namespaceURI, String sName, String qName) throws SAXException	{
			if(qName.equals("tr")) {
				if(address.getPropertyValue(ContactModel.ADDRESS1) != null && address.getPropertyValue(ContactModel.ADDRESS1).length() > 1) {
					Association assoc = new Association(ContactModel.ADDRESSES, 0, 0);
					assoc.setTargetEntity(address);
					row.getSourceAssociations().add(assoc);
					address.getTargetAssociations().add(assoc);
					getEntities().put(String.valueOf(address.getUuid()), address);
				}
				getEntities().put(String.valueOf(row.getUuid()), row);
				//else System.out.println("Tossed duplicate row");
				//tossRow = false;
			} else if(qName.equals("td")) {
				try {
					if(currField == 0) {
						row.addProperty(ContactModel.TITLE, data.toString().trim());
						String[] name = data.toString().split(",");
						if(name.length == 2) {
							name[0] = Character.toUpperCase(name[0].toLowerCase().charAt(0)) + name[0].toLowerCase().substring(1);
							row.addProperty(ContactModel.LAST_NAME, name[0].trim());
							row.addProperty(ContactModel.FIRST_NAME, name[1].trim());
							row.addProperty(SystemModel.NAME, name[0].trim()+", "+name[1].trim());
						} else if(name.length == 1) {
							row.addProperty(SystemModel.NAME, name[0].trim());
							row.setQName(ContactModel.ORGANIZATION);
						} else if(name.length == 3) {
							row.addProperty(ContactModel.LAST_NAME, name[0].trim());
							row.addProperty(ContactModel.FIRST_NAME, name[1].trim());
							row.addProperty(ContactModel.MIDDLE_NAME, name[2].trim());
							row.addProperty(SystemModel.NAME, name[0].trim()+", "+name[1].trim()+" "+name[2].trim());
						}
					} else if(currField == 1) {
						row.addProperty(ContactModel.GREETING, data.toString());
					} else if(currField == 2) {
						address.addProperty(ContactModel.ADDRESS1, data.toString());
					} else if(currField == 3) {
						address.addProperty(ContactModel.ADDRESS2, data.toString());					
					} else if(currField == 4) {
						if(data.toString().endsWith(",")) address.addProperty(ContactModel.CITY, data.toString().substring(0, data.toString().length()-1));
						else address.addProperty(ContactModel.CITY, data.toString());
					} else if(currField == 5) {
						address.addProperty(ContactModel.STATE, data.toString());
					} else if(currField == 6) {
						address.addProperty(ContactModel.COUNTRY, data.toString());
					} else if(currField == 7) {
						address.addProperty(ContactModel.ZIP, data.toString());
					} else if(currField == 8) {
						String[] date = data.toString().split("/");
						if(date.length != 3) date = data.toString().split("-");
						if(date.length == 3) {
							String month = date[0].length() == 1 ? "0"+date[0] : date[0];
							String day = date[1].length() == 1 ? "0"+date[1] : date[1];
							if(date.length == 3) row.addProperty(ContactModel.BIRTHDATE, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
						}
					} else if(currField == 9) {
						String[] date = data.toString().split("/");
						if(date.length != 3) date = data.toString().split("-");
						if(date.length == 3) {
							String month = date[0].length() == 1 ? "0"+date[0] : date[0];
							String day = date[1].length() == 1 ? "0"+date[1] : date[1];
							if(date.length == 3) row.addProperty(ContactModel.DEATHDATE, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
						}
					} else if(currField == 10) {
						String[] date = data.toString().split("/");
						if(date.length != 3) date = data.toString().split("-");
						if(date.length == 3) {
							String month = date[0].length() == 1 ? "0"+date[0] : date[0];
							String day = date[1].length() == 1 ? "0"+date[1] : date[1];
							if(date.length == 3) row.addProperty(ContactModel.LASTWROTE, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
						}
					} else if(currField == 11) {
						String[] date = data.toString().split("/");
						if(date.length != 3) date = data.toString().split("-");
						if(date.length == 3) {
							String month = date[0].length() == 1 ? "0"+date[0] : date[0];
							String day = date[1].length() == 1 ? "0"+date[1] : date[1];
							if(date.length == 3) row.addProperty(ContactModel.LASTSHIP, date[2]+"-"+month+"-"+day);//yyyy-MM-dd format
						}
					} else if(currField == 12) {
						if(data.toString().trim().length() > 0) {
							Entity restrictions = new Entity(SystemModel.NOTE);
							restrictions.addProperty(SystemModel.NOTE_TYPE, "Alternate Address");
							restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
							Association assoc = new Association(SystemModel.NOTES, 0, 0);
							assoc.setTargetEntity(restrictions);
							row.getSourceAssociations().add(assoc);
						}
					} else if(currField == 13) {
						if(data.toString().trim().length() > 0) {
							Entity restrictions = new Entity(ContactModel.PHONE);
							restrictions.addProperty(ContactModel.NUMBER, data.toString());
							Association assoc = new Association(ContactModel.PHONES, 0, 0);
							assoc.setTargetEntity(restrictions);
							row.getSourceAssociations().add(assoc);
						}
					} else if(currField == 14) {
						if(data.toString().trim().length() > 0) row.addProperty(ContactModel.FAX, data.toString());
					} else if(currField == 15) {
						if(data.toString().trim().length() > 0)  row.addProperty(ContactModel.SPOUSE, data.toString());
					} else if(currField == 16) {
						if(data.toString().trim().length() > 0) row.addProperty(ContactModel.NOTE, removeBreaks(data.toString()));
					} else if(currField == 17) {
						if(data.toString().trim().length() > 0) row.addProperty(ContactModel.STATUS, data.toString().toLowerCase());
					} else if(currField == 18) {
						if(data.toString().trim().length() > 0) row.addProperty(ContactModel.TYPE, data.toString().toLowerCase());
					} else if(currField == 19) {
						if(data.toString().trim().length() > 0) row.addProperty(ContactModel.BIO_SOURCES, data.toString());
					} else if(currField == 20) {
						if(data.toString().startsWith("Mr.")) row.addProperty(ContactModel.SALUTATION, "Mr.");
						else if(data.toString().startsWith("Mrs.")) row.addProperty(ContactModel.SALUTATION, "Mrs.");
						else if(data.toString().startsWith("Ms.")) row.addProperty(ContactModel.SALUTATION, "Ms.");
						else if(data.toString().startsWith("Dr.")) row.addProperty(ContactModel.SALUTATION, "Dr.");
					} else if(currField == 23) {
						row.addProperty(ContactModel.DATELIST, data.toString());
					} else if(currField == 31) {
						if(data.toString().trim().length() > 0) {
							Entity restrictions = new Entity(SystemModel.NOTE);
							restrictions.addProperty(SystemModel.NOTE_TYPE, "Work");
							restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
							Association assoc = new Association(SystemModel.NOTES, 0, 0);
							assoc.setTargetEntity(restrictions);
							row.getSourceAssociations().add(assoc);
						}
					} else if(currField == 31) {
						if(data.toString().trim().length() > 0) {
							Entity restrictions = new Entity(SystemModel.NOTE);
							restrictions.addProperty(SystemModel.NOTE_TYPE, "Contact");
							restrictions.addProperty(SystemModel.NOTE_CONTENT, data.toString());
							Association assoc = new Association(SystemModel.NOTES, 0, 0);
							assoc.setTargetEntity(restrictions);
							row.getSourceAssociations().add(assoc);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				currField++;
				inData = false;
			}
		}
	}
	
	protected String removeBreaks(String in) {
		return in.replaceAll("\\r\\n|\\r|\\n", " ");
	}
	public EntityService getEntityService() {
		return entityService;
	}
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
}
