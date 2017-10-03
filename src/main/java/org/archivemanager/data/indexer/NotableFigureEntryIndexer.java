package org.archivemanager.data.indexer;

import org.heed.openapps.dictionary.ClassificationModel;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.search.indexing.EntityIndexerSupport;
import org.heed.openapps.search.indexing.IndexEntity;
import org.heed.openapps.search.indexing.IndexEntityField;

public class NotableFigureEntryIndexer extends EntityIndexerSupport {

	
	@Override
	public IndexEntity index(Entity entity) throws InvalidEntityException {
		IndexEntity data = super.index(entity);
		
		if(entity.hasProperty(ClassificationModel.COLLECTION_NAME)) {
			data.getData().put("collection_name_e", new IndexEntityField("collection_name_e", entity.getPropertyValue(ClassificationModel.COLLECTION_NAME), false));
		}
		
		return data;
	}
}
