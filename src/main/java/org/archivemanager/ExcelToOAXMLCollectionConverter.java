package org.archivemanager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.heed.openapps.QName;
import org.heed.openapps.dictionary.ModelField;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.AssociationImpl;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityImpl;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.Property;


public class ExcelToOAXMLCollectionConverter {
	private Map<String,Entity> categories = new HashMap<String,Entity>();
	private Map<String,Entity> nodes = new HashMap<String,Entity>();
	
	private String directory = "C:/opt/programming/ArchiveManager/data/import/michael_douglas2";
		
		
	public static void main(String[] args) {
		ExcelToOAXMLCollectionConverter app = new ExcelToOAXMLCollectionConverter();
		app.process();
	}
	public void process() {			
		File dir = new File(directory);
		try {
			//FileWriter out = new FileWriter(new File("C:/opt/programming/ArchiveManager/data/import/michael_douglas.xml"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("C:/opt/programming/ArchiveManager/data/import/michael_douglas.xml")), "UTF-8"));
			Entity collection = new EntityImpl(RepositoryModel.COLLECTION);
			collection.setUid(java.util.UUID.randomUUID().toString());
			collection.setName("Michael Douglas");
			for(File file : dir.listFiles()) {
				System.out.println("--------------"+file.getName()+"--------------");
				Workbook wb = WorkbookFactory.create(file);
				Sheet sheet = wb.getSheetAt(0);
				for(Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
					Row data = rit.next();
					if(data.getRowNum() > 0) {
						String box = clean(getStringCellValue(data.getCell(0)));
						String catalog = clean(getStringCellValue(data.getCell(1)));
						String category = clean(getStringCellValue(data.getCell(2)));
						String desc = clean(getStringCellValue(data.getCell(3)));
						String fileName = clean(getStringCellValue(data.getCell(4)));
						String name = clean(getStringCellValue(data.getCell(5)));
						String credit = clean(getStringCellValue(data.getCell(10)));
						
						QName qname = RepositoryModel.ITEM;					
						if(category != null) {
							if(category.equals("Script") || category.equals("Speech"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "manuscript");
							else if(category.equals("Correspondence"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "correspondence");
							else if(category.equals("Awards") || category.equals("Awards & Honors") || category.equals("Memorabilia") || 
									category.equals("Wardrobe") || category.equals("Wardrobe - Costume") || category.equals("Wardrobe - Personal"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "memorabilia");
							else if(category.equals("Press & Publicity"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "professional");
							else if(category.equals("Book"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "printed_material");
							else if(category.startsWith("3/4") || category.equals("35mm film") || category.equals("35mm film") || 
									category.equals("Betacam") || category.equals("DVD") || category.equals("MiniDV") || 
									category.equals("Multimedia") || category.equals("Other") || category.equals("Pal") || category.equals("PAL") ||
									category.equals("VHS") || category.equals("Video"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "video");
							else if(category.equals("CD"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "audio");
							else if(category.equals("Negative") || category.equals("Photograph") || category.equals("Slide") || 
									category.equals("Transparency"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "photographs");
							else if(category.equals("Art") || category.equals("Artwork"))
								qname = new QName(RepositoryModel.NAMESPACE_ARCHIVE, "artwork");						
						}
						//System.out.println("box:"+box+" catalog:"+catalog+" category:"+category+" desc:"+desc+" fileName:"+fileName+" name:"+name+" credit:"+credit);
						Entity entityCategory = categories.get(category);
						if(entityCategory == null) {
							entityCategory = new EntityImpl(RepositoryModel.CATEGORY);
							entityCategory.setUid(java.util.UUID.randomUUID().toString());
							entityCategory.setName(category);
							nodes.put(entityCategory.getUid(), entityCategory);
							collection.getSourceAssociations().add(new AssociationImpl(new QName(RepositoryModel.NAMESPACE_ARCHIVE, "categories"), collection.getUid(), entityCategory.getUid()));
							categories.put(category, entityCategory);
						}
						
						Entity entity = new EntityImpl(qname);
						entity.setUid(UUID.randomUUID().toString());
						entity.setName(name);
						entity.addProperty(ModelField.TYPE_LONGTEXT, RepositoryModel.DESCRIPTION, desc);
						//Box #, Catalog #, FileName, and Photo Credit
						String comment = "";
						if(box != null && box.length() > 0) 
							comment += box+"\n";
						if(catalog != null && catalog.length() > 0) 
							comment += " "+catalog+"\n";
						if(fileName != null && fileName.length() > 0) 
							comment += " "+fileName+"\n";
						if(credit != null && credit.length() > 0) {
							comment += " "+credit;							
						}
						entity.addProperty(ModelField.TYPE_LONGTEXT, new QName(RepositoryModel.NAMESPACE_ARCHIVE, "comments"), comment.trim());
						entityCategory.getSourceAssociations().add(new AssociationImpl(new QName(RepositoryModel.NAMESPACE_ARCHIVE, "items"), entityCategory.getUid(), entity.getUid()));
						nodes.put(entity.getUid(), entity);						
					}
				}			
			}
			out.write("<?xml version='1.0' encoding='utf-8'?><import>"+toXml(collection, true, true)+"</import>");
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String toXml(Entity entity, boolean printSources, boolean printTargets) throws InvalidEntityException {
		StringBuffer buff = new StringBuffer("<node uid='"+entity.getUid()+"' qname='"+entity.getQName().toString()+"'>");
		buff.append("<name><![CDATA["+clean(entity.getName())+"]]></name>");
		for(Property property : entity.getProperties()) {
			if(property.getValue() != null && property.getValue().toString().length() > 0)
				buff.append("<property type='"+property.getType()+"' qname='"+property.getQName()+"'><![CDATA["+clean(String.valueOf(property.getValue()))+"]]></property>");
		}
		buff.append("</node>");
		if(printSources) {
			List<Association> source_associations = entity.getSourceAssociations();
			for(Association association : source_associations) {
				Entity targetEntity = nodes.get(association.getTargetUid());
				buff.append("<association qname='"+association.getQName()+"' sourceUid='"+entity.getUid()+"' targetUid='"+targetEntity.getUid()+"'>");
				for(Property property : association.getProperties()) {
					if(property.getValue() != null && property.getValue().toString().length() > 0)
						buff.append("<property type='"+property.getType()+"' qname='"+property.getQName()+"'><![CDATA["+clean(String.valueOf(property.getValue()))+"]]></property>");
				}				
				buff.append("</association>");					
				buff.append(toXml(targetEntity, true, false));	
				
			}
		}
		return buff.toString();
	}
	protected String clean(String in) {
		return in.trim();
	}
	protected void addProperty(Entity entity, QName qname, Object data) {
		try {
			entity.addProperty(qname, data);
		} catch(Exception e) {
			System.out.println("error adding property qname:"+qname+" value:"+data);
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
	
}
