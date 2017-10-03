package org.archivemanager.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.heed.openapps.QName;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.data.FormatInstructions;
import org.heed.openapps.entity.DefaultExportProcessor;


public class CollectionContentExportProcessor extends DefaultExportProcessor {
	private static final long serialVersionUID = 7176155657808313347L;
	
		
	@Override
	public Object export(FormatInstructions instructions, Entity entity) throws InvalidEntityException {
		if(instructions.getFormat().equals(FormatInstructions.FORMAT_CSV)) {
			return toCsv(entity);
		} else if(instructions.getFormat().equals(FormatInstructions.FORMAT_JSON)) {
			Map<String,Object> data = super.toMap(entity, instructions);			
			List<Entity> path = getComponentPath(entity);			
			if(path.size() > 0) {
				List<Map<String,Object>> pathList = new ArrayList<Map<String,Object>>();
				long collectionId = 0;
				String collectionName = null;
				String collectionUrl = null;
				Long parentId = null;
				for(int i=0; i < path.size(); i++) {
					Entity node = path.get(i);
					String title = node.getName();
					String dateExpression = node.getPropertyValue(RepositoryModel.DATE_EXPRESSION);
					if(!node.getQName().equals(RepositoryModel.REPOSITORY)) {
						Map<String,Object> nodeMap = new HashMap<String,Object>();
						nodeMap.put("id", node.getId());
						nodeMap.put("type", node.getQName().getLocalName());
						nodeMap.put("parent", parentId);
						if(title != null  && !title.equals(""))
							nodeMap.put("title", title);
						else if(dateExpression != null)
							nodeMap.put("title", dateExpression);
						pathList.add(nodeMap);
						parentId = node.getId();
					}
					if(node.getQName().equals(RepositoryModel.COLLECTION)) {
						collectionId = node.getId();
						collectionName = node.getName();
						collectionUrl = node.getPropertyValue(new QName("openapps.org_repository_1.0", "url"));
					}
				}
				data.put("path", pathList);
				if(collectionId > 0) data.put("collection_id", collectionId);
				if(collectionUrl != null) data.put("collection_url", collectionUrl);
				if(collectionName != null) data.put("collection_name", collectionName);
			}			
			return data;
		} else {
			List<Entity> path = getComponentPath(entity);
			
			if(path.size() > 0) {
				getBuffer().append("<path>");
				long collectionId = 0;
				String collectionName = null;
				String collectionUrl = null;
				Long parentId = null;
				for(int i=0; i < path.size(); i++) {
					Entity node = path.get(i);
					String title = node.getName();
					String dateExpression = node.getPropertyValue(RepositoryModel.DATE_EXPRESSION);
					if(!node.getQName().equals(RepositoryModel.REPOSITORY)) {
						if(title != null  && !title.equals("")) getBuffer().append("<node id='"+node.getId()+"' type='"+node.getQName().getLocalName()+"' parent='"+parentId+"'><title><![CDATA["+title+"]]></title></node>");
						else if(dateExpression != null) getBuffer().append("<node id='"+node.getId()+"' type='"+node.getQName().getLocalName()+"' parent='"+parentId+"'><title>"+dateExpression+"</title></node>");
						parentId = node.getId();
					}
					if(node.getQName().equals(RepositoryModel.COLLECTION)) {
						collectionId = node.getId();
						collectionName = node.getName();
						collectionUrl = node.getPropertyValue(new QName("openapps.org_repository_1.0", "url"));
					}
				}
				getBuffer().append("</path>").toString();
				if(collectionId > 0) getBuffer().append("<collection_id>"+collectionId+"</collection_id>");
				if(collectionUrl != null) getBuffer().append("<collection_url>"+collectionUrl+"</collection_url>");
				if(collectionName != null) getBuffer().append("<collection_name><![CDATA["+collectionName+"]]></collection_name>");
			}		
			
			return toXml(entity, instructions.printSources(), instructions.printTargets());
		} 
	}
	
	protected List<Entity> getComponentPath(Entity comp) {
		List<Entity> path = new ArrayList<Entity>();
		if(comp != null) {
			Association parent = comp.getTargetAssociation(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
			while(parent != null) {
				try {
					Entity p = getEntityService().getEntity(parent.getSource());
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
		
}
