package org.archivemanager.data;



public class DigitalObjectCleaner {

	
	public void clean() {
		/*
		Long id1 = uids.get(uid);
		Long id2 = node.getId();
		try {
			Entity e1 = getEntity(id1);
			Entity e2 = getEntity(id2);
			boolean dirty = false;
			if(e1.getQname().equals(ContentModel.FOLDER)) {
				for(Property p1 : e1.getProperties()) {
					Property p2 = e2.getProperty(p1.getQname());
					if(!p1.getValue().equals(p2.getValue())) {
						if(!p2.isEmpty() && p1.isEmpty()) {
							p1.setValue(p2.getValue());
							dirty = true;
							log.info("adding property:"+p2.getQname().getLocalName()+" value:"+p2.getValue());
						} else if(!p1.isEmpty() && p2.isEmpty()) {
							
						} else if(e2.getId() > e1.getId()) {
							p1.setValue(p2.getValue());
							dirty = true;
							log.info("adding property:"+p2.getQname().getLocalName()+" value:"+p2.getValue());
						}									
					}
				}
				if(dirty) {
					updateEntity(e1, false);
					removeEntity(e2.getId());
					uids.put(uid, e1.getId());
				}
			}						
		} catch(Exception e) {
			e.printStackTrace();
		}
		//log.warn("duplicate nodes with uid:"+uid);
		*/
	}
}
