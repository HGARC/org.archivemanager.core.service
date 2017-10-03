package org.archivemanager.util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.archivemanager.model.Audio;
import org.archivemanager.model.Collection;
import org.archivemanager.model.DigitalObject;
import org.archivemanager.model.Entry;
import org.archivemanager.model.Item;
import org.archivemanager.model.NamedEntity;
import org.archivemanager.model.Note;
import org.archivemanager.model.PathNode;
import org.archivemanager.model.Subject;
import org.archivemanager.model.Video;
import org.archivemanager.model.WebLink;
import org.heed.openapps.QName;
import org.heed.openapps.SystemModel;
import org.heed.openapps.dictionary.ClassificationModel;
import org.heed.openapps.content.ContentModel;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.util.HTMLUtility;


public class EntityRepositoryModelUtil extends RepositoryModelUtilSupport {
	private EntityService entityService;
	
	
	public EntityRepositoryModelUtil(EntityService entityService) {
		this.entityService = entityService;
	}
	
	public Collection getCollection(Entity entity, boolean sources) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Collection collection = new Collection(id, title, description, contentType);
		
		if(entity.hasProperty(RepositoryModel.URL) && entity.getPropertyValue(RepositoryModel.URL).length() > 0) 
			collection.setUrl(entity.getPropertyValue(RepositoryModel.URL));
		if(entity.hasProperty(RepositoryModel.BIOGRAPHICAL_NOTE)) collection.setBioNote(entity.getPropertyValue(RepositoryModel.BIOGRAPHICAL_NOTE));
		if(entity.hasProperty(RepositoryModel.SCOPE_NOTE)) collection.setScopeNote(entity.getPropertyValue(RepositoryModel.SCOPE_NOTE));
		
		if(entity.hasProperty(RepositoryModel.DATE_EXPRESSION)) {
			collection.setDateExpression(entity.getPropertyValue(RepositoryModel.DATE_EXPRESSION));
			if(collection.getTitle() == null) collection.setTitle(collection.getDateExpression());			
		}
		if(entity.hasProperty(RepositoryModel.BEGIN_DATE)) collection.setBegin(entity.getPropertyValue(RepositoryModel.BEGIN_DATE));
		if(entity.hasProperty("end")) collection.setEnd(entity.getPropertyValue(RepositoryModel.END_DATE));		
		if(entity.hasProperty("container") ) collection.setContainer(entity.getPropertyValue(RepositoryModel.CONTAINER));		
		
		if(sources && entity.getSourceAssociations().size() > 0) {
			for(int i=0; i < entity.getSourceAssociations().size(); i++) {
				Association association = entity.getSourceAssociations().get(i);
				Entity targetEntity = association.getTargetEntity();
				try {
					if(targetEntity == null) targetEntity = entityService.getEntity(association.getTarget());
					if(targetEntity != null) {	
						if(association.getQName().getLocalName().equals("notes")) {					
							if(targetEntity.hasProperty(SystemModel.NOTE_TYPE)) {
							String type = targetEntity.getPropertyValue(SystemModel.NOTE_TYPE);
							String content = targetEntity.getPropertyValue(SystemModel.NOTE_CONTENT);
							if(type.equals("General note") || type.equals("General Physical Description note") ||
								type.equals("Table of Contents")) {
								collection.getNotes().add(new Note("", type, content));
							} 
						}					
						} else if(association.getQName().getLocalName().equals("named_entities")) {
							String localName = targetEntity.getQName().getLocalName();
							NamedEntity name = getNamedEntity(targetEntity);
							if(localName.equals("person")) collection.getPeople().add(name);
							if(localName.equals("corporation")) collection.getCorporations().add(name);
						} else if(association.getQName().getLocalName().equals("subjects")) {
							Subject subject = getSubject(targetEntity);
							collection.getSubjects().add(subject);
						} else if(association.getQName().getLocalName().equals("files")) {
							DigitalObject file = getDigitalObject(targetEntity);
							collection.getDigitalObjects().add(file);
						} else if(association.getQName().getLocalName().equals("web_links")) {
							WebLink file = getWebLink(targetEntity);
							collection.getWeblinks().add(file);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		/*
		if(entity.has("series")) {
			JSONArray seriesList = entity.getJSONArray("series");
			for(int i=0; i < seriesList.length(); i++) {
				Series seriesObj = new Series(seriesList.getJSONObject(i));
				series.add(seriesObj);
			}
		}
		*/		
		return collection;
	}
	public Entry getEntry(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Entry entry = new Entry(id, title, description, contentType);
		
		if(entity.hasProperty(ClassificationModel.COLLECTION_NAME)) 
			entry.setCollection(entity.getPropertyValue(ClassificationModel.COLLECTION_NAME));
		if(entity.hasProperty(ClassificationModel.ITEMS)) {
			String itemDescription = (String)entity.getProperty(ClassificationModel.ITEMS).getValue();
			itemDescription = itemDescription.replace("\n", "<br>");
			entry.setDescription(itemDescription);
		}
		
		if(entity.getTargetAssociations().size() > 0) {
			for(int i=0; i < entity.getTargetAssociations().size(); i++) {
				Association association = entity.getTargetAssociations().get(i);
				if(association.getQName().getLocalName().equals("entries")) {
					Entity sourceEntity = association.getSourceEntity();
					try {
						if(sourceEntity == null) sourceEntity = entityService.getEntity(association.getSource());
						entry.setNamedEntity(String.valueOf(association.getSource()));
						if(sourceEntity != null) {											
							entry.setTitle(sourceEntity.getName());
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return entry;
	}
	public Item getItem(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Item item = new Item(id, title, description, contentType);
		if(entity.hasProperty(RepositoryModel.LANGUAGE)) item.setLanguage(getLanguageLabel(entity.getPropertyValue(RepositoryModel.LANGUAGE)));
		else item.setLanguage("English");
		if(entity.hasProperty(RepositoryModel.CONTAINER)) item.setContainer(entity.getPropertyValue(RepositoryModel.CONTAINER));
		if(entity.hasProperty(RepositoryModel.SUMMARY) && entity.getPropertyValue(RepositoryModel.SUMMARY).length() > 0) item.setSummary(entity.getPropertyValue(RepositoryModel.SUMMARY));
		if(entity.hasProperty(RepositoryModel.DATE_EXPRESSION)) {
			if(item.getTitle() == null) item.setTitle(entity.getPropertyValue(RepositoryModel.DATE_EXPRESSION));
			item.setDateExpression(entity.getPropertyValue(RepositoryModel.DATE_EXPRESSION));
		}
		List<Entity> path = getComponentPath(entity);
		if(path.size() > 0) {
			for(int i=0; i < path.size(); i++) {
				Entity node = path.get(i);
				PathNode pathNode = new PathNode(String.valueOf(node.getId()), node.getName());
				item.getPath().add(pathNode);
				if(node.getQName().equals(RepositoryModel.COLLECTION)) {
					item.setCollectionId(String.valueOf(node.getId()));
					item.setCollectionName(node.getName());
					item.setCollectionUrl(node.getPropertyValue(new QName("openapps.org_repository_1.0", "url")));
				}
			}
		}
		if(entity.getSourceAssociations().size() > 0) {
			for(int i=0; i < entity.getSourceAssociations().size(); i++) {
				Association association = entity.getSourceAssociations().get(i);
				Entity targetEntity = association.getTargetEntity();
				try {
					if(targetEntity == null) targetEntity = entityService.getEntity(association.getTarget());
					if(targetEntity != null) {	
						if(association.getQName().getLocalName().equals("notes")) {					
							if(targetEntity.hasProperty(SystemModel.NOTE_TYPE)) {
							String type = targetEntity.getPropertyValue(SystemModel.NOTE_TYPE);
							String content = targetEntity.getPropertyValue(SystemModel.NOTE_CONTENT);
							if(type.equals("General note") || type.equals("General Physical Description note") ||
								type.equals("Table of Contents")) {
								item.getNotes().add(new Note("", type, content));
							}
							if(type.equals("Language of Materials note")) {
								item.setNativeContent(content);
							}
						}					
						} else if(association.getQName().getLocalName().equals("named_entities")) {
							String localName = targetEntity.getQName().getLocalName();
							NamedEntity name = getNamedEntity(targetEntity);
							if(localName.equals("person")) item.getPeople().add(name);
							if(localName.equals("corporation")) item.getCorporations().add(name);
						} else if(association.getQName().getLocalName().equals("subjects")) {
							Subject subject = getSubject(targetEntity);
							item.getSubjects().add(subject);
						} else if(association.getQName().getLocalName().equals("files")) {
							DigitalObject file = getDigitalObject(targetEntity);
							item.getDigitalObjects().add(file);
						} else if(association.getQName().getLocalName().equals("web_links")) {
							WebLink file = getWebLink(targetEntity);
							item.getWeblinks().add(file);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}		
		return item;
	}
	public NamedEntity getNamedEntity(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = HTMLUtility.removeTags(entity.getName());
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		NamedEntity name = new NamedEntity(id, title, description, contentType);
		
		if(entity.hasProperty(RepositoryModel.FUNCTION)) name.setFunction(entity.getPropertyValue(RepositoryModel.FUNCTION));
		if(entity.hasProperty(ClassificationModel.RULE)) name.setRole(entity.getPropertyValue(ClassificationModel.RULE));
		if(entity.hasProperty(ClassificationModel.NOTE)) name.setNote(entity.getPropertyValue(ClassificationModel.NOTE));
		if(entity.hasProperty(ClassificationModel.DATES)) name.setDates(entity.getPropertyValue(ClassificationModel.DATES));
		if(entity.hasProperty(ClassificationModel.SOURCE)) name.setSource(entity.getPropertyValue(ClassificationModel.SOURCE));
				
		if(entity.getSourceAssociations().size() > 0) {
			for(int i=0; i < entity.getSourceAssociations().size(); i++) {
				Association association = entity.getSourceAssociations().get(i);
				if(association.getQName().getLocalName().equals("entries")) {
					Entity targetEntity = association.getTargetEntity();
					try {
						if(targetEntity == null) targetEntity = entityService.getEntity(association.getTarget());
						Entry entry = getEntry(targetEntity);
						name.getEntries().add(entry);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(entity.getTargetAssociations().size() > 0) {
			for(int i=0; i < entity.getTargetAssociations().size(); i++) {
				Association association = entity.getTargetAssociations().get(i);
				if(association.getQName().getLocalName().equals("named_entities")) {
					Entity sourceEntity = association.getSourceEntity();
					try {
						if(sourceEntity == null) sourceEntity = entityService.getEntity(association.getSource());
						Collection collection = getCollection(sourceEntity, false);
						name.getCollections().add(collection);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return name;
	}
	public Audio getAudio(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Audio audio = new Audio(id, title, description, contentType);
		if(entity.hasProperty(RepositoryModel.DATE_EXPRESSION)) {
			audio.setDateExpression(entity.getPropertyValue(RepositoryModel.DATE_EXPRESSION));
		}
		if(entity.getSourceAssociations().size() > 0) {
			for(int i=0; i < entity.getSourceAssociations().size(); i++) {
				Association association = entity.getSourceAssociations().get(i);
				Entity targetEntity = association.getTargetEntity();
				try {
					if(targetEntity == null) targetEntity = entityService.getEntity(association.getTarget());
					if(targetEntity != null) {
						if(association.getQName().getLocalName().equals("web_links")) {
							WebLink file = getWebLink(targetEntity);
							audio.getWeblinks().add(file);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		for(WebLink link : audio.getWeblinks()) {
			if(link.getType().equals("avatar"))
				audio.setAvatar(link.getUrl());
			else if(link.getType().equals("rendition"))
				audio.setRendition(link.getUrl());
		}
		return audio;
	}
	public Subject getSubject(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Subject subject = new Subject(id, title, description, contentType);
		if(entity.hasProperty(ClassificationModel.TYPE)) subject.setType(entity.getPropertyValue(ClassificationModel.TYPE));
		if(entity.hasProperty(ClassificationModel.SOURCE)) subject.setSource(entity.getPropertyValue(ClassificationModel.TYPE));
		List<Association> collectionAssociations = entity.getTargetAssociations(RepositoryModel.COLLECTION);
		for(Association collectionAssociation : collectionAssociations) {
			Entity sourceEntity = collectionAssociation.getSourceEntity();
			try {
				if(sourceEntity == null) sourceEntity = entityService.getEntity(collectionAssociation.getSource());
				if(sourceEntity != null) {
					Collection collection = getCollection(sourceEntity, false);
					subject.getCollections().add(collection);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return subject;
	}
	public Video getVideo(Entity entity) {
		String id = String.valueOf(entity.getId());
		String title = entity.getName();
		String description = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
		String contentType = cleanContentType(entity.getQName().getLocalName());
		Video video = new Video(id, title, description, contentType);
		if(entity.hasProperty(RepositoryModel.DATE_EXPRESSION)) {
			video.setDateExpression(entity.getPropertyValue(RepositoryModel.DATE_EXPRESSION));
		}
		if(entity.getSourceAssociations().size() > 0) {
			for(int i=0; i < entity.getSourceAssociations().size(); i++) {
				Association association = entity.getSourceAssociations().get(i);
				Entity targetEntity = association.getTargetEntity();
				try {
					if(targetEntity == null) targetEntity = entityService.getEntity(association.getTarget());
					if(targetEntity != null) {
						if(association.getQName().getLocalName().equals("web_links")) {
							WebLink file = getWebLink(targetEntity);
							video.getWeblinks().add(file);
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		for(WebLink link : video.getWeblinks()) {
			if(link.getType().equals("avatar"))
				video.setAvatar(link.getUrl());
			else if(link.getType().equals("rendition"))
				video.setRendition(link.getUrl());
		}
		return video;
	}
	public DigitalObject getDigitalObject(Entity entity) {
		DigitalObject file = new DigitalObject();
		file.setId(String.valueOf(entity.getId()));
		file.setUuid(entity.getUuid());
		file.setTitle(entity.getName());						
		if(entity.hasProperty(ContentModel.TYPE)) file.setType(entity.getPropertyValue(ContentModel.TYPE));
		//if(source.has("group")) file.setGroup(source.getString("group"));
		//if(source.has("order")) file.setOrder(source.getInt("order"));
		return file;
	}
	public WebLink getWebLink(Entity entity) {
		WebLink file = new WebLink();
		file.setId(String.valueOf(entity.getId()));
		if(entity.hasProperty(SystemModel.URL)) {
			file.setUrl(entity.getPropertyValue(SystemModel.URL));
			int endIdx = file.getUrl().indexOf("/", 7);
			if(endIdx > -1) file.setDomain(file.getUrl().substring(0, endIdx));
		}
		file.setTitle(entity.getName());
		if(entity.hasProperty(ContentModel.TYPE)) file.setType(entity.getPropertyValue(ContentModel.TYPE));
		return file;
	}
	protected List<Entity> getComponentPath(Entity comp) {
		List<Entity> path = new ArrayList<Entity>();
		if(comp != null) {
			Association parent = comp.getTargetAssociation(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
			while(parent != null) {
				try {
					Entity p = entityService.getEntity(parent.getSource());
					path.add(p);
					parent = p.getTargetAssociation(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		Collections.reverse(path);
		return path;
	}
	protected String getLanguageLabel(String in) {
		if(in.equals("en")) return "English";
		if(in.equals("ar")) return "Arabic";
		if(in.equals("zh")) return "Chinese";
		if(in.equals("cs")) return "Czech";
		if(in.equals("da")) return "Danish";
		if(in.equals("nl")) return "Dutch";
		if(in.equals("en")) return "English";
		if(in.equals("fi")) return "Finnish";
		if(in.equals("fr")) return "French";
		if(in.equals("de")) return "German";
		if(in.equals("el")) return "Greek";
		if(in.equals("he")) return "Hebrew";
		if(in.equals("hu")) return "Hungarian";
		if(in.equals("is")) return "Icelandic";
		if(in.equals("it")) return "Italian";
		if(in.equals("ja")) return "Japanese";
		if(in.equals("ko")) return "Korean";
		if(in.equals("no")) return "Norwegian";
		if(in.equals("pl")) return "Polish";
		if(in.equals("pt")) return "Portugese";
		if(in.equals("ru")) return "Russian";
		if(in.equals("es")) return "Spanish";
		if(in.equals("sv")) return "Swedish";
		if(in.equals("th")) return "Thai";
		if(in.equals("tr")) return "Turkish";
		if(in.equals("yi")) return "Yiddish";
		return in;
	}
}
