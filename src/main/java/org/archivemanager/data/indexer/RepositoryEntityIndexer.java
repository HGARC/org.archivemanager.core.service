package org.archivemanager.data.indexer;

import java.util.logging.Logger;

import org.heed.openapps.dictionary.ClassificationModel;
import org.heed.openapps.QName;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.Property;
import org.heed.openapps.search.indexing.EntityIndexerSupport;
import org.heed.openapps.search.indexing.IndexEntity;
import org.heed.openapps.search.indexing.IndexEntityField;


public class RepositoryEntityIndexer extends EntityIndexerSupport {
	private final static Logger log = Logger.getLogger(RepositoryEntityIndexer.class.getName());
	
	
	@Override
	public IndexEntity index(Entity entity) throws InvalidEntityException {
		IndexEntity data = super.index(entity);		
						
		Property summary = entity.getProperty(RepositoryModel.SUMMARY);
		if(summary != null) {
			String value = (String)summary.getValue();
			if(value != null && value.length() > 0 && !value.equals("<br>") && !value.startsWith("<!--[if gte mso 9]>"))
				appendFreeText(value, data);
		}
		
		if(entity.getQName().equals(RepositoryModel.COLLECTION))
			data.getData().put("sort_", new IndexEntityField("sort_", 4L, false));
		else if(entity.getQName().equals(ClassificationModel.PERSON) || entity.getQName().equals(ClassificationModel.CORPORATION) || entity.getQName().equals(ClassificationModel.FAMILY))
			data.getData().put("sort_", new IndexEntityField("sort_", 3L, false));
		else if(entity.getQName().equals(ClassificationModel.SUBJECT))
			data.getData().put("sort_", new IndexEntityField("sort_", 2L, false));
		else
			data.getData().put("sort_", new IndexEntityField("sort_", 0L, false));
		Association parent_assoc = entity.getTargetAssociation(RepositoryModel.CATEGORIES);
		if(parent_assoc == null) parent_assoc = entity.getTargetAssociation(RepositoryModel.ITEMS);
		if(parent_assoc != null && parent_assoc.getSource() != null) {
			Entity parent = getEntityService().getEntity(parent_assoc.getSource());
			data.getData().put("parent_id", new IndexEntityField("parent_id", parent.getId(), false));
			data.getData().put("parent_qname", new IndexEntityField("parent_qname", parent.getQName().toString(), false));
			StringBuffer buff = new StringBuffer();
			while(parent != null) {
				buff.insert(0, parent.getId()+" ");
				
				parent_assoc = parent.getTargetAssociation(RepositoryModel.CATEGORIES);
				if(parent_assoc == null) parent_assoc = parent.getTargetAssociation(RepositoryModel.ITEMS);
				if(parent_assoc != null && parent_assoc.getSource() != null) {
					parent = getEntityService().getEntity(parent_assoc.getSource());
				} else parent = null;
			}
			data.getData().put("path", new IndexEntityField("path", buff.toString().trim(), true));
		}
		for(Association assoc : entity.getSourceAssociations()) {			
			Entity node = assoc.getTarget() != null ? getEntityService().getEntity(assoc.getTarget()) : getEntityService().getEntity(assoc.getTargetUid());
			if(node != null) {
				QName nodeQname = node.getQName();
				if(nodeQname.equals(ClassificationModel.SUBJECT) || nodeQname.equals(ClassificationModel.PERSON) || nodeQname.equals(ClassificationModel.CORPORATION)) {
					Object nodeName = node.getProperty(SystemModel.NAME.toString());
					if(nodeName != null) appendFreeText(nodeName.toString(), data);
				} else if(nodeQname.equals(SystemModel.NOTE)) {
					String type = node.hasProperty(SystemModel.NOTE_TYPE.toString()) ? node.getPropertyValue(SystemModel.NOTE_TYPE) : null;
					if(type != null) {
						if(type.equals("General note") || type.equals("Abstract") || 
								type.equals("General Physical Description note") || 
								type.equals("Table of Contents")) {
							String property = node.hasProperty(SystemModel.NOTE_CONTENT.toString()) ? node.getPropertyValue(SystemModel.NOTE_CONTENT) : null;
							if(property != null) {
								String content = property.toString();
								if(content != null && content.length() > 0) {
									data.getData().put(type.toLowerCase(), new IndexEntityField(type.toLowerCase(), content, true));
									appendFreeText(content, data);
								} 
							}
						}
					}
				}
			} else log.info("node not found for : "+assoc.getTargetUid());			
		}
		return data;
	}
	protected void appendFreeText(String freeText, IndexEntity data) throws InvalidEntityException {
		String textVal = freeText.toString();
		IndexEntityField currentValue = data.getData().get("freetext");
		if(currentValue != null) {
			textVal = textVal+" "+currentValue.getValue();
		}
		if(textVal != null && textVal.length() > 0) {
			textVal = textVal.replace(",", " ");
			textVal = textVal.replaceAll("\\<.*?>"," ");
		}
		try {
			if(textVal != null && textVal.length() > 0) {
				data.getData().put("freetext", new IndexEntityField("freetext", textVal.trim().toLowerCase(), true));
			}
		} catch(Exception e) {
			throw new InvalidEntityException("");
		}
	}
	@Override
	public void deindex(QName qname, Entity entity) {
		// TODO Auto-generated method stub
		
	}
	
	protected QName getQName(Long node) {
		QName qname = null;
		try {
			if(node != null) {
				String qnameStr = getNodeService().hasNodeProperty(node, "qname") ? (String)getNodeService().getNodeProperty(node, "qname") :"";
				qname = QName.createQualifiedName(qnameStr);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return qname;
	}
	
}
