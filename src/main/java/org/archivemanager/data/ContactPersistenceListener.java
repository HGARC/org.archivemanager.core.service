package org.archivemanager.data;

import org.heed.openapps.dictionary.ContactModel;
import org.heed.openapps.QName;
import org.heed.openapps.SystemModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.DefaultEntityPersistenceListener;
import org.heed.openapps.entity.Entity;


public class ContactPersistenceListener extends DefaultEntityPersistenceListener {

	
	@Override
	public void onBeforeAdd(Entity entity) {
		try {
			if(entity.getQName().equals(ContactModel.INDIVIDUAL)) {
				String name = "";
				if(entity.getPropertyValue(ContactModel.LAST_NAME) != null) name += entity.getPropertyValue(ContactModel.LAST_NAME);
				if(entity.getPropertyValue(ContactModel.FIRST_NAME) != null) {
					if(name.length() > 0) name += ", ";
					name += entity.getPropertyValue(ContactModel.FIRST_NAME);
				}
				if(entity.getPropertyValue(ContactModel.MIDDLE_NAME) != null) name += " "+entity.getPropertyValue(ContactModel.MIDDLE_NAME);
				entity.addProperty(SystemModel.NAME, name);
			} else {
			
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAfterAdd(Entity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeUpdate(Entity oldValue, Entity newValue) {
		try {
			if(newValue.getQName().equals(ContactModel.INDIVIDUAL)) {
				String name = "";
				if(newValue.getPropertyValue(ContactModel.LAST_NAME) != null) name += newValue.getPropertyValue(ContactModel.LAST_NAME);
				if(newValue.getPropertyValue(ContactModel.FIRST_NAME) != null) {
					if(name.length() > 0) name += ", ";
					name += newValue.getPropertyValue(ContactModel.FIRST_NAME);
				}
				if(newValue.getPropertyValue(ContactModel.MIDDLE_NAME) != null) name += " "+newValue.getPropertyValue(ContactModel.MIDDLE_NAME);
				newValue.addProperty(SystemModel.NAME, name);
			} else {
			
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
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
	public Entity extractEntity(long node, QName entityQname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onBeforeAssociationAdd(Association assoc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterAssociationAdd(Association assoc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationUpdate(Association assoc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterAssociationUpdate(Association assoc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBeforeAssociationDelete(Association assoc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAfterAssociationDelete(Association assoc) {
		// TODO Auto-generated method stub
		
	}

}
