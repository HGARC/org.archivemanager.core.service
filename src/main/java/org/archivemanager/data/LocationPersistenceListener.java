package org.archivemanager.data;

import javax.servlet.http.HttpServletRequest;

import org.heed.openapps.QName;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.DefaultEntityPersistenceListener;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.InvalidPropertyException;


public class LocationPersistenceListener extends DefaultEntityPersistenceListener {

	
	@Override
	public void onBeforeUpdate(Entity oldValue, Entity newValue) {
		addTitle(newValue);
	}

	@Override
	public void onBeforeAdd(Entity entity) {
		addTitle(entity);
		
	}
	protected void addTitle(Entity entity) {
		String building = entity.getPropertyValue(RepositoryModel.BUILDING);
		String floor = entity.getPropertyValue(RepositoryModel.FLOOR);
		String aisle = entity.getPropertyValue(RepositoryModel.AISLE);
		String bay = entity.getPropertyValue(RepositoryModel.BAY);
		String name = "";
		if(building != null && building.length() > 0) name += building;
		if(floor != null && floor.length() > 0) name += " Floor "+floor;
		if(aisle != null && aisle.length() > 0) name += " Aisle "+aisle;
		if(bay != null && bay.length() > 0) name += " Bay "+bay;
		try {
			entity.addProperty(SystemModel.NAME, name.trim());
		} catch(InvalidPropertyException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onAfterAdd(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterUpdate(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeDelete(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterDelete(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Entity extractEntity(HttpServletRequest arg0, QName arg1)
			throws InvalidEntityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity extractEntity(long id, QName entityQname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onAfterAssociationAdd(Association arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterAssociationDelete(Association arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterAssociationUpdate(Association arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationAdd(Association arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationDelete(Association arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationUpdate(Association arg0) {
		// TODO Auto-generated method stub
		
	}

}
