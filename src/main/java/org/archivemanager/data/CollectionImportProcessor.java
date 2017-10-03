/*
 * Copyright (C) 2009 Heed Technology Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.archivemanager.data;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.heed.openapps.QName;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.data.FileImportProcessor;


public abstract class CollectionImportProcessor extends FileImportProcessor {
	private static final long serialVersionUID = 8184120712127639960L;
	protected EntityService entityService;
	private QName qname;
	
	protected Map<QName,QName> qnames = new HashMap<QName,QName>();
	private Entity root;
		
	public CollectionImportProcessor(){}
	public CollectionImportProcessor(String name) {
		super(RepositoryModel.COLLECTION.toString(), name);
	}
	public String cleanString(String in) {		
		return in;
	}
	public QName getQname(QName qname) {
		return qname;
	}
	@Override
	public Entity getRoot() {
		return root;
	}
	public Entity getEntityById(String id) {
		return getEntities().get(id);
	}
	public void addEntity(Entity parent, Entity child) throws InvalidEntityException {
		if(child == null) throw new InvalidEntityException("null child");
		if(child.getId() == null) child.setUuid(UUID.randomUUID().toString());
		if(parent != null) {
			if(parent.getQName().equals(RepositoryModel.COLLECTION) && child.getQName().equals(RepositoryModel.CATEGORY))
				addAssoc(parent, child, RepositoryModel.CATEGORIES);
			else if(parent.getQName().equals(RepositoryModel.CATEGORY) && child.getQName().equals(RepositoryModel.CATEGORY))
				addAssoc(parent, child, RepositoryModel.CATEGORIES);
			else addAssoc(parent, child, RepositoryModel.ITEMS);
		}
		else throw new InvalidEntityException("null parent");
		getEntities().put(child.getUuid(), child);
	}
	public void addAssoc(Entity source, Entity target, QName qname) {
		Association assoc = new Association(qname, source.getQName(), target.getQName());
		if(assoc != null) {
			assoc.setSourceUid(source.getUuid());
			assoc.setTargetUid(target.getUuid());
			target.getTargetAssociations().add(assoc);
			source.getSourceAssociations().add(assoc);
		}
	}
	public void setRoot(Entity root) {
		this.root = root;
		if(root.getId() == null) root.setUuid(UUID.randomUUID().toString());
		getEntities().put(root.getUuid(), root);
	}
	public void setEntityService(EntityService nodeSvc) {
		this.entityService = nodeSvc;
	}
	public EntityService getEntityService() {
		return entityService;
	}
	public QName getQName() {
		return qname;
	}
	public void setQName(QName qname) {
		this.qname = qname;
	}
	public Map<QName,QName> getQnames() {
		return qnames;
	}
	public void setQnames(Map<QName,QName> qnames) {
		this.qnames = qnames;
	}
	
}
