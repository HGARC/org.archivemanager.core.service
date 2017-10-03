package org.archivemanager.data;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.InvalidQualifiedNameException;
import org.heed.openapps.util.DecodingUtility;
import org.heed.openapps.util.NumberUtility;

@SuppressWarnings("unused")
public class DefaultTextCollectionImportProcessor extends CollectionImportProcessor {
	private static final long serialVersionUID = -6794322443617301822L;
	private Map<String,Entity> series = new HashMap<String,Entity>();
	private Map<String,Entity> subseries = new HashMap<String,Entity>();
	private Map<String,Entity> containerMap = new HashMap<String,Entity>();
	
	private String box;
	private String pakg;
	private String fldr;
	private String title;
	private String date;
	
	//private Entity categories;
	//private Entity containers;
	private Entity lastSeries;
	private Entity lastSubseries;
	private Entity lastFile;
	private Entity level1;
	private Entity level2;
	private Entity level3;
	private Entity level4;
	private Entity level5;
	
	public DefaultTextCollectionImportProcessor(){
		super();
	}
	public DefaultTextCollectionImportProcessor(String name) {
		super(name);
	}
	
	public void process(InputStream stream, Map<String, Object> properties) throws Exception {
		getEntities().clear();
		String data = DecodingUtility.convertStreamToString(stream);
		parse(data);
	}
	public Entity parse(String content) throws Exception{
		Entity collection = new Entity();
		collection.setUuid(java.util.UUID.randomUUID().toString());
		collection.setQName(RepositoryModel.CATEGORY);
		//collection.setStore(getStoreid());
		setRoot(collection);
		//categories = new Entity(getQname(SystemModel.CATEGORIES));
		//addEntity(collection,categories);
		//containers = new Entity(getQname(RepositoryModel.CONTAINERS));
		//addEntity(collection,containers);
		//Entity accessions = new Entity(getQname(RepositoryModel.ACCESSIONS));
		//addEntity(collection,accessions);
		box = null;
		pakg = null;
		fldr = null;
		date = null;
		series.clear();
		subseries.clear();
		containerMap.clear();
		try {
			String str;
			BufferedReader reader = new BufferedReader(new StringReader(content));
			while((str = reader.readLine()) != null) {
				str = str.trim();
				if(str.length() > 0 && !str.equals("")) {					
					if(getRoot().getName() == null) {
						title = str;
						getRoot().setName(str);
					}
					int pos = 0;
					/** 
					 * Level 5 : First Char '(', Body i,ii,iii,iv,v etc., LastChar ')'
					 * Level 6 : First Char '(', Body a,b,c,d,e,f,g etc., LastChar ')'
					 */
					if(str.startsWith("(")) {
						pos++;
						StringBuffer marker = new StringBuffer();
						while(str.charAt(pos) != ')') {
							marker.append(str.charAt(pos));
							pos++;
						}
						String mark = marker.toString();
						if(mark.equals("i") || mark.equals("ii") || mark.equals("iii") || mark.equals("iv") || mark.equals("v") || mark.equals("vi") || mark.equals("vii") || mark.equals("viii") || mark.equals("ix")  || mark.equals("x") || mark.equals("xi")  || mark.equals("xii") || mark.equals("xiii") || mark.equals("xiv") || mark.equals("xv")) 
							parseLevel5(clean(str.substring(pos+1)));
						else
							parseLevel6(clean(str.substring(pos+1)));
					/** 
					 * Level 3 : Digit with trailing '.'
					 */
					} else if(Character.isDigit(str.charAt(pos))) {
						while(Character.isDigit(str.charAt(pos)) && str.length() > pos+1) pos++;
						if(str.charAt(pos) == '.')
							parseLevel3(clean(str.substring(pos+1)));
					/**
					 * Level 1 : Character, Upper Case, Roman Numeral (starts with I,V,X), Trailing '.'
					 * Level 2 : Character, Upper Case, Non-Roman Numeral, Trailing '.'
					 */
					} else if(Character.isUpperCase(str.charAt(pos))) {
						if(str.startsWith("Box") || str.startsWith("BOX")) parseBox(str);
						else if(str.startsWith("Package") || str.startsWith("PACKAGE")) parsePackage(str);
						else if(str.startsWith("DATE::")) parseDate(str);
						else if(str.startsWith("I") || str.startsWith("V") || str.startsWith("X")){
							while(Character.isUpperCase(str.charAt(pos))) pos++;
							if(str.charAt(pos) == '.') 
								parseLevel1(clean(str.substring(pos+1)));
						} else  {
							while(Character.isUpperCase(str.charAt(pos))) pos++;
							if(str.charAt(pos) == '.') 
								parseLevel2(clean(str.substring(pos+1)));
						}
					/**
					 * Level 4 : Character, Lower Case, Trailing '.'
					 */
					} else if(Character.isLowerCase(str.charAt(pos))) {
						
						while(Character.isLowerCase(str.charAt(pos))) pos++;
						if(str.charAt(pos) == '.') 
							parseLevel4(clean(str.substring(pos+1)));
						
					}
				}
		    }
			for(Entity node : getEntities().values()) {
				Association parentAssoc = node.getParent();
				if(parentAssoc != null) {
					//Entity parent = getEntityById(parentAssoc.getSourceUid());
					if(node.getProperty(RepositoryModel.CATEGORY_LEVEL) != null && node.getParent().getProperty(RepositoryModel.CATEGORY_LEVEL) != null) {
						//System.out.println(node.getProperty(RepositoryModel.COMPONENT_LEVEL)+" - "+node.getParent().getProperty(RepositoryModel.COMPONENT_LEVEL)+" - "+node.getChildren().size());
						if(node.getProperty(RepositoryModel.CATEGORY_LEVEL).toString().equals("subseries") && 
							node.getParent().getProperty(RepositoryModel.CATEGORY_LEVEL).toString().equals("series") && 
							node.getSourceAssociations().size() == 0) {
								node.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "file");
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(title != null && date != null) getRoot().addProperty(SystemModel.NAME, title+" - "+date);
		else if(title != null) getRoot().addProperty(SystemModel.NAME, title);
		return collection;
	}
		
	protected void parseBox(String str) {
		if(str.startsWith("Box")) {
			pakg = null;
			String boxStr = null;
			if(str.length() > 4 && Character.isDigit(str.charAt(4)))
				if(str.length() > 5 && Character.isDigit(str.charAt(5)))
					if(str.length() > 6 && Character.isDigit(str.charAt(6)))
						if(str.length() > 7 && Character.isDigit(str.charAt(7)))
							System.out.println("Box greater than 1000 encountered.");
						else boxStr = str.substring(4, 7);
					else boxStr = str.substring(4, 6);
				else boxStr = str.substring(4, 5);
			else boxStr = str.substring(4, 4);
			if(NumberUtility.isInteger(boxStr)) {
				box = boxStr;
			}
		}
	}
	protected void parsePackage(String str) {
		if(str.startsWith("Package")) {
			String pakgStr = null;
			if(str.length() > 8 && Character.isDigit(str.charAt(8)))
				if(str.length() > 9 && Character.isDigit(str.charAt(9)))
					if(str.length() > 10 && Character.isDigit(str.charAt(10)))
						if(str.length() > 11 && Character.isDigit(str.charAt(11)))
							System.out.println("Package greater than 1000 encountered.");
						else pakgStr = str.substring(8, 11);
					else pakgStr = str.substring(8, 10);
				else pakgStr = str.substring(8, 9);
			else pakgStr = str.substring(8, 8);
			if(NumberUtility.isInteger(pakgStr)) {
				pakg = pakgStr;
			}
		}
	}
	protected String parseFolder(String in) {
		Pattern folderFinder = Pattern.compile("\\[F. [1-9 | \\-]+\\]");
		Pattern folderFinder2 = Pattern.compile("[1-9 | \\-]+");
		Matcher m = folderFinder.matcher(in);
		while(m.find()) {
			final int gc = m.groupCount();
            for ( int i = 0; i <= gc; i++ ) {
                String match = m.group(i);
                Matcher m2 = folderFinder2.matcher(match);
                m2.find();
                fldr = m2.group(0);
                in = m.replaceAll("");
            }
		}
		return in;
	}
	protected String parseEnvelope(String in) {
		Pattern folderFinder = Pattern.compile("\\[Env. [1-9\\-]+\\]");
		Pattern folderFinder2 = Pattern.compile("[1-9]+");
		Matcher m = folderFinder.matcher(in);
		while(m.find()) {
			final int gc = m.groupCount();
            for ( int i = 0; i <= gc; i++ ) {
                String match = m.group(i);
                Matcher m2 = folderFinder2.matcher(match);
                m2.find();
                fldr = m2.group(0);
                in = m.replaceAll("");
            }
		}
		return in;
	}
	protected void parseDate(String str) {
		if(str.startsWith("DATE::")) {
			date = str.substring(6);
			box = null;
			pakg = null;
			fldr = null;
		}
	}
	protected void parseLevel1(String in) throws Exception {
		String title = parseSeriesTitle(in);
		Entity newSeries = series.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.CATEGORY);
			newSeries.addProperty(RepositoryModel.CATEGORY_LEVEL, "series");
			if(title != null) newSeries.setName(title);
			addEntity(getRoot(), newSeries);
			lastSeries = newSeries;
			series.put(title, newSeries);
			level1 = newSeries;
		}
		//setContainer(newSeries);
	}
	protected String parseSeriesTitle(String in) {
		if(in.toLowerCase().contains("manuscript")) return "Manuscript";
		if(in.toLowerCase().contains("rinted material")) return "Printed Material";
		if(in.toLowerCase().contains("correspondence")) return "Correspondence";
		if(in.toLowerCase().contains("legal material")) return "Legal Material";
		if(in.toLowerCase().contains("subject files")) return "Subject Files";
		if(in.toLowerCase().contains("personal memorabilia")) return "Personal Memorabilia";
		if(in.toLowerCase().contains("scrapbook")) return "Scrapbooks";
		if(in.toLowerCase().contains("research material")) return "Research Material";
		if(in.toLowerCase().contains("professional material")) return "Professional Material";
		if(in.toLowerCase().contains("financial material")) return "Financial Material";
		String out = in.replace("(continued)", "").replace("(correspondence)", "").replace("(continued.)", "").trim();
		return out;
	}
	protected void parseLevel2(String in) throws Exception{
		String title = parseFolder(in);
		Entity newSeries = subseries.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.CATEGORY);
			newSeries.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "subseries");
			if(title != null) newSeries.setName(title);
			if(date != null) newSeries.addProperty(getQname(RepositoryModel.ACCESSION_DATE), date);
			addEntity(lastSeries, newSeries);
			lastSubseries = newSeries;
			subseries.put(title, newSeries);
			level2 = newSeries;
		}
		setContainer(newSeries);
	}
	protected void parseLevel3(String in) throws Exception {
		String title = parseFolder(in);
		Entity newSeries = series.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.CATEGORY);
			newSeries.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "file");
			if(title != null) newSeries.setName(title);
			if(date != null) newSeries.addProperty(getQname(RepositoryModel.ACCESSION_DATE), date);
			addEntity(lastSubseries, newSeries);
			lastFile = newSeries;
			level3 = newSeries;
		}
		setContainer(newSeries);
	}
	protected void parseLevel4(String in) throws Exception {
		String title = parseFolder(in);
		Entity newSeries = series.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.ITEM);
			newSeries.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "item");
			if(title != null) newSeries.setName(title);
			if(date != null) newSeries.addProperty(getQname(RepositoryModel.ACCESSION_DATE), date);
			addEntity(lastFile, newSeries);
			level4 = newSeries;
		}
		setContainer(newSeries);
	}
	protected void parseLevel5(String in) throws Exception {
		String title = parseFolder(in);
		Entity newSeries = series.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.ITEM);
			newSeries.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "item");
			if(title != null) newSeries.setName(title);
			if(date != null) newSeries.addProperty(getQname(RepositoryModel.ACCESSION_DATE), date);
			addEntity(level4, newSeries);
			level3.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "subseries");
			level4.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "file");
			level5 = newSeries;
		}
		setContainer(newSeries);
	}
	protected void parseLevel6(String in) throws Exception {
		String title = parseFolder(in);
		Entity newSeries = series.get(title);
		if(newSeries == null) {
			newSeries = new Entity();
			newSeries.setUuid(java.util.UUID.randomUUID().toString());
			newSeries.setQName(RepositoryModel.ITEM);
			newSeries.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "item");
			if(title != null) newSeries.setName(title);
			if(date != null) newSeries.addProperty(getQname(RepositoryModel.ACCESSION_DATE), date);
			level3.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "subseries");
			level4.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "subseries");
			level5.addProperty(getQname(RepositoryModel.CATEGORY_LEVEL), "file");
			addEntity(level5, newSeries);
		}
		setContainer(newSeries);
	}
	protected void setContainer(Entity comp) throws Exception {
		Entity boxEntity = null;
		Entity pakgEntity = null;
		Entity fldrEntity = null;
		StringBuffer container = new StringBuffer();
		if(box != null) {
			//boxEntity = containerMap.get("box"+box);
			container.append("box "+box);
			/*
			if(boxEntity == null) {
				boxEntity = new Entity(RepositoryModel.CONTAINER);
				addEntity(containers, boxEntity);
				boxEntity.addProperty(RepositoryModel.TITLE, "Box "+box);
				boxEntity.addProperty(RepositoryModel.CONTAINER_TYPE, "box");
				containerMap.put("box"+box, boxEntity);
			}
			*/
			if(pakg != null) {
				//pakgEntity = containerMap.get("pakg"+pakg);
				/*
				if(pakgEntity == null) {
					pakgEntity = new Entity(RepositoryModel.CONTAINER);
					addEntity(boxEntity, pakgEntity);
					pakgEntity.addProperty(RepositoryModel.TITLE, "Package "+pakg);
					pakgEntity.addProperty(RepositoryModel.CONTAINER_TYPE, "package");
					containerMap.put("pakg"+pakg, pakgEntity);
				}
				*/
				container.append(" package "+pakg);
			}
			if(fldr != null) {
				//fldrEntity = containerMap.get("fldr"+fldr);
				/*
				if(fldrEntity == null) {
					fldrEntity = new Entity(RepositoryModel.CONTAINER);
					if(pakg != null) addEntity(pakgEntity, fldrEntity);
					else addEntity(boxEntity, fldrEntity);
					fldrEntity.addProperty(RepositoryModel.TITLE, "Folder "+fldr);
					fldrEntity.addProperty(RepositoryModel.CONTAINER_TYPE, "folder");
					containerMap.put("fldr"+fldr, fldrEntity);
				}
				*/
				container.append(" folder "+fldr);
			}
			//if(fldrEntity != null && noParentAssignedToContainer(comp,fldrEntity)) comp.getTargetAssociations().add(new EntityAssoc(RepositoryModel.COMPONENT_CONTAINERS, fldrEntity, comp));
			//else if(pakgEntity != null && noParentAssignedToContainer(comp,pakgEntity)) comp.getTargetAssociations().add(new EntityAssoc(RepositoryModel.COMPONENT_CONTAINERS, pakgEntity, comp));
			//else if(boxEntity != null && noParentAssignedToContainer(comp,boxEntity)) comp.getTargetAssociations().add(new EntityAssoc(RepositoryModel.COMPONENT_CONTAINERS, boxEntity, comp));
			comp.addProperty(getQname(RepositoryModel.CONTAINER), container.toString().trim());
		}
	}
	protected boolean noParentAssignedToContainer(Entity node, Entity container) throws InvalidEntityException, InvalidQualifiedNameException {
		if(node.getParent() != null) {
			Association parentAssoc = node.getParent();
			if(parentAssoc != null) {
				Entity parent = entityService.getEntity(parentAssoc.getSource());
				if(parent != null && parent.containsTargetAssociation(RepositoryModel.CONTAINERS)) {
					List<Association> containers = parent.getTargetAssociations(RepositoryModel.CONTAINERS);
					for(Association cont : containers) {
						Entity sourceEntity = entityService.getEntity(cont.getSource());
						if(sourceEntity.equals(container)) return false;
					}
				}
			}
		}
		return true;
	}
	protected boolean startsWith(String in, String[] list) {
		for(int i=0; i < list.length; i++) {
			if(in.startsWith(list[i])) return true;
		}
		return false;
	}
	protected String clean(String in) {
		if(in.endsWith(".")) in = in.substring(0, in.length()-1);
		return in.trim();
	}
}