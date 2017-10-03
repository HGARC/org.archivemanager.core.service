package org.archivemanager.data;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.heed.openapps.QName;
import org.heed.openapps.User;
import org.heed.openapps.dictionary.DataDictionary;
import org.heed.openapps.dictionary.Model;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.dictionary.ModelField;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.Property;
import org.heed.openapps.search.SearchModel;
import org.heed.openapps.search.data.SearchEntityPersistenceListener;


public class CollectionPersistenceListener extends SearchEntityPersistenceListener {
	
	
	@Override
	public Entity extractEntity(HttpServletRequest request, QName entityQname) throws InvalidEntityException {
		Entity entity = null;
		try {
			String id = request.getParameter("id");
			if(id != null && id.length() > 0) {
				entity = entityService.getEntity(Long.valueOf(id));
				String user = request.getParameter("user");
				if(user != null && user.length() > 0 && !user.equals("0")) {
					entity.setUser(Long.valueOf(user));
				}
			} else {
				User user = securityService.getCurrentUser(request);
				if(user != null) {
					String uid = request.getParameter("uid");
					entity = new Entity(entityQname);		
					entity.setCreated(System.currentTimeMillis());
					entity.setModified(System.currentTimeMillis());				
					if(uid != null && uid.length() > 0) entity.setUuid(uid);
					else entity.setUuid(UUID.randomUUID().toString());
					entity.setUser(securityService.getCurrentUser(request).getId());
				}
			}
			if(entity != null) {
				String name = request.getParameter("name");
				if(name != null) entity.setName(name);
				DataDictionary dictionary = dictionaryService.getDataDictionary(entity.getDictionary());
				for(ModelField field : dictionary.getModelFields(entity.getQName())) {
					try {
						String value = request.getParameter(field.getQName().getLocalName());
						if(value == null) value = request.getParameter(field.getQName().getLocalName());
						if(value != null && value.length() > 0 && !value.equals("null")) {
							if(field.getType() == ModelField.TYPE_DATE) 
								entity.addProperty(Property.DATE, field.getQName(), value);
							else if(field.getType() == ModelField.TYPE_INTEGER) 
								entity.addProperty(Property.INTEGER, field.getQName(), value);
							else if(field.getType() == ModelField.TYPE_LONG) 
								entity.addProperty(Property.LONG, field.getQName(), value);
							else entity.addProperty(field.getQName(), value);				
						} else {
							if(entity.hasProperty(field.getQName())) {
								entity.getProperty(field.getQName()).setValue("");
							}
						}
					} catch(Exception e) {
						e.toString();
					}
				}			
				String repository = request.getParameter("repository");
				if(repository != null && repository.length() > 0 && !repository.equals("null")) {
					List<Association> assocs = entity.getAssociations(RepositoryModel.COLLECTIONS);
					Entity target = entityService.getEntity(Long.valueOf(repository));
					if(assocs.size() == 0) {						
						Association a = new Association(RepositoryModel.COLLECTIONS, target,  entity);
						entity.getTargetAssociations().add(a);
					} else {
						Association a = assocs.get(0);
						a.setSourceEntity(target);
						a.setSource(target.getId());
						a.setTargetEntity(entity);
						a.setTarget(entity.getId());
						try {
							entityService.updateAssociation(a);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return entity;
		} catch(Exception e) {
			throw new InvalidEntityException("", e);
		}
	}

	@Override
	public void onAfterAdd(Entity entity) {
		DataDictionary sourceDictionary = dictionaryService.getDataDictionary(entity.getDictionary());
		Model sourceModel = sourceDictionary.getModel(entity.getQName());
		if(sourceModel.isEntityIndexed()) {
			searchService.update(entity, false);
		}
	}
	
	@Override
	public void onAfterUpdate(Entity entity) {
		DataDictionary sourceDictionary = dictionaryService.getDataDictionary(entity.getDictionary());
		Model sourceModel = sourceDictionary.getModel(entity.getQName());
		if(sourceModel.isEntityIndexed()) {
			searchService.update(entity, false);
		}
	}
	
	@Override
	public void onAfterDelete(Entity entity) {
		DataDictionary sourceDictionary = dictionaryService.getDataDictionary(entity.getDictionary());
		Model sourceModel = sourceDictionary.getModel(entity.getQName());
		if(sourceModel.isEntityIndexed()) {
			searchService.remove(entity.getId());
		}
	}

	@Override
	public void onAfterAssociationAdd(Association association) {
		if(association.getQName().equals(SearchModel.DEFINITIONS)) {
			try {
				Entity collection = entityService.getEntity(association.getSource());
				searchService.reload(collection);
			} catch(InvalidEntityException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onAfterAssociationDelete(Association association) {
		if(association.getQName().equals(SearchModel.DEFINITIONS)) {
			try {
				Entity collection = entityService.getEntity(association.getSource());
				searchService.reload(collection);
			} catch(InvalidEntityException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public Entity extractEntity(long id, QName qname) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onAfterAssociationUpdate(Association association) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBeforeAdd(Entity arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationAdd(Association association) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBeforeAssociationDelete(Association association) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBeforeAssociationUpdate(Association association) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBeforeDelete(Entity arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onBeforeUpdate(Entity arg0, Entity arg1) {
		// TODO Auto-generated method stub
		
	}	
	
}
