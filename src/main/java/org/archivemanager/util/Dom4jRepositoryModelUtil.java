package org.archivemanager.util;

import java.util.List;

import org.archivemanager.model.Collection;
import org.archivemanager.model.DigitalObject;
import org.archivemanager.model.Entry;
import org.archivemanager.model.NamedEntity;
import org.archivemanager.model.Note;
import org.archivemanager.model.PathNode;
import org.archivemanager.model.Subject;
import org.archivemanager.model.WebLink;
import org.dom4j.Element;

public class Dom4jRepositoryModelUtil extends RepositoryModelUtilSupport {

	
	/*
	public static Collection getCollection(Element entity) {
		if(entity.attribute("id") != null) this.id = entity.attribute("id").getText();			
		if(entity.element("name") != null) this.title = entity.element("name").getText();
		if(entity.element("description") != null) this.description = entity.element("description").getText();
		if(entity.attribute("localName") != null) this.contentType = cleanContentType(entity.attribute("localName").getText());
		if(title != null) title = title.replace("<br>", "");
		if(description != null) description = description.replace("<br>", "");
		
		if(entity.element("url") != null) this.url = entity.element("url").getText();
		if(entity.element("bio_note") != null) this.bioNote = entity.element("bio_note").getText();
		else if(entity.element("title") != null) setTitle(entity.element("title").getText());
		if(entity.element("scope_note") != null) this.scopeNote = entity.element("scope_note").getText();
		if(entity.element("date_expression") != null) {
			if(getTitle() == null) setTitle(entity.element("date_expression").getText());
			this.dateExpression = entity.element("date_expression").getText();
		}
		if(entity.element("container") != null) this.container = entity.element("container").getText();
		
		Element source_associations = entity.element("source_associations");
		if(source_associations != null) {
			Element notesElement = source_associations.element("notes");
			if(notesElement != null) {
				List<Element> notesList = notesElement.elements();
				if(notesList != null) {
					for(Element note : notesList) {
						if(note.element("type") != null) {
							String type = note.element("type").getText();
							if(type.equals("General note") || type.equals("General Physical Description note") ||
									type.equals("Table of Contents")) {
								notes.add(new Note("", type, note.element("content").getText()));
							} 
						}
					}
				}
			}
			Element namesElement = source_associations.element("named_entities");
			if(namesElement != null) {
				List<Element> namesList = namesElement.elements();
				if(namesList != null) {
					for(Element nameNode : namesList) {
						String localName = nameNode.attributeValue("localName");
						NamedEntity name = new NamedEntity(nameNode);
						if(nameNode.attribute("target_id") != null) name.setId(nameNode.attributeValue("target_id"));
						
						if(localName.equals("person")) people.add(name);
						if(localName.equals("corporation")) corporations.add(name);
					}
				}
			}
			Element subjectsElement = source_associations.element("subjects");
			if(subjectsElement != null) {
				List<Element> subjectsList = subjectsElement.elements();
				if(subjectsList != null) {
					for(Element subjectNode : subjectsList) {
						Subject subject = new Subject(subjectNode);
						if(subjectNode.attributeValue("target_id") != null) subject.setId(subjectNode.attributeValue("target_id"));
						subjects.add(subject);
					}
				}
			}
			Element filesElement = source_associations.element("files");
			if(filesElement != null) {
				List<Element> filesList = filesElement.elements();
				if(filesList != null) {
					for(Element fileNode : filesList) {
						DigitalObject file = new DigitalObject();
						if(fileNode.attribute("target_id") != null) file.setId(fileNode.attribute("target_id").getText());
						if(fileNode.attribute("uuid") != null) file.setUuid(fileNode.attribute("uuid").getText());
						if(fileNode.element("title") != null) file.setTitle(fileNode.element("title").getText());
						if(fileNode.element("mimetype") != null) file.setMimetype(fileNode.element("mimetype").getText());
						if(fileNode.element("type") != null) file.setType(fileNode.element("type").getText());
						if(fileNode.element("group") != null) file.setGroup(fileNode.element("group").getText());
						if(fileNode.element("order") != null) file.setOrder(Integer.valueOf(fileNode.element("order").getText()));
						digitalObjects.add(file);
					}
				}
			}
			Element weblinksElement = source_associations.element("web_links");
			if(weblinksElement != null) {
				List<Element> weblinksList = weblinksElement.elements();
				if(weblinksList != null) {
					for(Element fileNode : weblinksList) {
						WebLink file = new WebLink();
						if(fileNode.attribute("id") != null) file.setId(fileNode.attribute("target_id").getText());
						if(fileNode.element("url") != null) {
							file.setUrl(fileNode.element("url").getText());
							int endIdx = file.getUrl().indexOf("/", 7);
							if(endIdx > -1) file.setDomain(file.getUrl().substring(0, endIdx));
						}
						if(fileNode.element("title") != null) file.setTitle(fileNode.element("title").getText());
						if(fileNode.element("type") != null) file.setType(fileNode.element("type").getText());
						weblinks.add(file);
					}
				}
			}
		}
	}
	public static Entry getEntry(Element entity) {
		if(entity.element("collection_name") != null) this.collection = entity.element("collection_name").getText();
		if(entity.element("items") != null) setDescription(entity.element("items").getText());
		namedEntity = entity.attributeValue("source_id");
		Element target_associations = entity.element("target_associations");
		if(target_associations != null) {
			Element subjectsElement = target_associations.element("entries");
			if(subjectsElement != null) {
				List<Element> subjectsList = subjectsElement.elements();
				if(subjectsList != null) {
					for(Element node : subjectsList) {
						namedEntity = node.attributeValue("source_id");
						if(getTitle() == null) setTitle(node.element("name").getText());
						
					}
				}
			}
		}
	}
	public Item(Element entity) {
		super(entity);
		if(entity.element("container") != null) this.container = entity.element("container").getText();
		if(entity.element("summary") != null && entity.element("summary").getText().length() > 0)
			this.summary = entity.element("summary").getText();
		if(entity.element("collection_id") != null) this.collectionId = entity.element("collection_id").getText();
		if(entity.element("collection_name") != null) this.collectionName = entity.element("collection_name").getText();
		if(entity.element("collection_url") != null) this.collectionUrl = entity.element("collection_url").getText();
		if(entity.element("date_expression") != null) {
			if(getTitle() == null) setTitle(entity.element("date_expression").getText());
			this.dateExpression = entity.element("date_expression").getText();
		}
		
		Element pathElement = entity.element("path");
		if(pathElement != null) {
			List<Element> nodesList = pathElement.elements("node");
			if(nodesList != null) {
				for(Element nodeElement : nodesList) {
					PathNode pathNode = new PathNode(nodeElement);
					path.add(pathNode);
				}
			}
		}
		
		Element source_associations = entity.element("source_associations");
		if(source_associations != null) {
			Element notesElement = source_associations.element("notes");
			if(notesElement != null) {
				List<Element> notesList = notesElement.elements();
				if(notesList != null) {
					for(Element note : notesList) {
						if(note.element("type") != null) {
							String type = note.element("type").getText();
							if(type.equals("General note") || type.equals("General Physical Description note") ||
									type.equals("Table of Contents")) {
								notes.add(new Note("", type, note.element("content").getText()));
							} 
						}
					}
				}
			}
			Element namesElement = source_associations.element("named_entities");
			if(namesElement != null) {
				List<Element> namesList = namesElement.elements();
				if(namesList != null) {
					for(Element nameNode : namesList) {
						String localName = nameNode.attributeValue("localName");
						NamedEntity name = new NamedEntity(nameNode);
						if(nameNode.attribute("target_id") != null) name.setId(nameNode.attributeValue("target_id"));
						
						if(localName.equals("person")) people.add(name);
						if(localName.equals("corporation")) corporations.add(name);
					}
				}
			}
			Element subjectsElement = source_associations.element("subjects");
			if(subjectsElement != null) {
				List<Element> subjectsList = subjectsElement.elements();
				if(subjectsList != null) {
					for(Element subjectNode : subjectsList) {
						Subject subject = new Subject(subjectNode);
						if(subjectNode.attributeValue("target_id") != null) subject.setId(subjectNode.attributeValue("target_id"));
						subjects.add(subject);
					}
				}
			}
			Element filesElement = source_associations.element("files");
			if(filesElement != null) {
				List<Element> filesList = filesElement.elements();
				if(filesList != null) {
					for(Element fileNode : filesList) {
						DigitalObject file = new DigitalObject();
						if(fileNode.attribute("target_id") != null) file.setId(fileNode.attribute("target_id").getText());
						if(fileNode.attribute("uuid") != null) file.setUuid(fileNode.attribute("uuid").getText());
						if(fileNode.element("title") != null) file.setTitle(fileNode.element("title").getText());
						if(fileNode.element("mimetype") != null) file.setMimetype(fileNode.element("mimetype").getText());
						if(fileNode.element("type") != null) file.setType(fileNode.element("type").getText());
						if(fileNode.element("group") != null) file.setGroup(fileNode.element("group").getText());
						if(fileNode.element("order") != null) file.setOrder(Integer.valueOf(fileNode.element("order").getText()));
						digitalObjects.add(file);
					}
				}
			}
			Element weblinksElement = source_associations.element("web_links");
			if(weblinksElement != null) {
				List<Element> weblinksList = weblinksElement.elements();
				if(weblinksList != null) {
					for(Element fileNode : weblinksList) {
						WebLink file = new WebLink();
						if(fileNode.attribute("id") != null) file.setId(fileNode.attribute("target_id").getText());
						if(fileNode.element("url") != null) {
							file.setUrl(fileNode.element("url").getText());
							int endIdx = file.getUrl().indexOf("/", 7);
							if(endIdx > -1) file.setDomain(file.getUrl().substring(0, endIdx));
						}
						if(fileNode.element("title") != null) file.setTitle(fileNode.element("title").getText());
						if(fileNode.element("type") != null) file.setType(fileNode.element("type").getText());
						weblinks.add(file);
					}
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void setPathElement(Element element) {
		List<Element> pathList = element.elements("node");
		for(Element pathNode : pathList) {
			PathNode n = new PathNode();
			if(pathNode.attribute("id") != null) n.setId(pathNode.attribute("id").getText());
			if(pathNode.element("title") != null) n.setTitle(pathNode.element("title").getText());
			path.add(n);
		}
	}
	@SuppressWarnings("unchecked")
	public NamedEntity(Element entity) {
		super(entity);
		if(entity.element("function") != null) setFunction(entity.element("function").getText());
		if(entity.element("rule") != null) setRole(entity.element("rule").getText());
		if(entity.element("note") != null) setFunction(entity.element("note").getText());
		
		Element source_associations = entity.element("source_associations");
		if(source_associations != null) {
			Element namesElement = source_associations.element("entries");
			if(namesElement != null) {
				List<Element> namesList = namesElement.elements();
				if(namesList != null) {
					for(Element name : namesList) {
						if(name.attributeValue("localName").equals("entry")) {
							Entry entry = new Entry(name);
							entries.add(entry);
						}
					}
				}
			}
		}
		
		Element target_associations = entity.element("target_associations");
		if(target_associations != null) {
			Element namesElement = target_associations.element("named_entities");
			if(namesElement != null) {
				List<Element> namesList = namesElement.elements();
				if(namesList != null) {
					for(Element name : namesList) {
						if(name.attributeValue("localName").equals("collection")) {
							Collection collection = new Collection(name);
							collection.setId(name.attributeValue("source_id"));
							collections.add(collection);
						} 
					}
				}
			}
		}
	}
	public Audio(Element entity) {
		super(entity);
		
		for(WebLink link : getWeblinks()) {
			if(link.getType().equals("avatar"))
				avatar = link.getUrl();
			else if(link.getType().equals("rendition"))
				rendition = link.getUrl();
		}
	}
	@SuppressWarnings("unchecked")
	public Subject(Element entity) {
		super(entity);
		if(entity.element("type") != null) this.type = entity.element("type").getText();
		if(entity.element("source") != null) this.type = entity.element("source").getText();
		
		Element target_associations = entity.element("target_associations");
		if(target_associations != null) {
			Element subjectsElement = target_associations.element("subjects");
			if(subjectsElement != null) {
				List<Element> subjectsList = subjectsElement.elements();
				if(subjectsList != null) {
					for(Element subject : subjectsList) {
						if(subject.attributeValue("localName").equals("collection")) {
							Collection collection = new Collection(subject);
							collection.setId(subject.attributeValue("source_id"));
							collections.add(collection);
						}
					}
				}
			}
		}
	}
	public Video(Element entity) {
		super(entity);		
		for(WebLink link : getWeblinks()) {
			if(link.getType().equals("avatar"))
				avatar = link.getUrl();
			else if(link.getType().equals("rendition"))
				rendition = link.getUrl();
		}
	}
	*/
}
