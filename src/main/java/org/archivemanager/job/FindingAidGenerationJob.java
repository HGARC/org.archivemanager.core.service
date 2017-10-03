package org.archivemanager.job;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archivemanager.util.ContainerSorter;
import org.archivemanager.util.FindingAidUtil;
import org.heed.openapps.QName;
import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.InvalidPropertyException;
import org.heed.openapps.InvalidQualifiedNameException;
import org.heed.openapps.reporting.ReportingDataSource;
import org.heed.openapps.reporting.ReportingService;
import org.heed.openapps.scheduling.JobSupport;
import org.heed.openapps.util.HTMLUtility;


public class FindingAidGenerationJob extends JobSupport {
	private static final long serialVersionUID = -2529433334917054265L;
	protected EntityService entityService;
	protected ReportingService reportingService;
	protected Long collectionId;
	protected String format;
	protected String title;
	protected Map<String,Object> parameters;
	protected FindingAidUtil util = new FindingAidUtil();
	
	
	public FindingAidGenerationJob(Long collectionId, String format, String title) {
		this.collectionId = collectionId;
		this.format = format;
		this.title = title;
	}
	
	@Override 
	public void execute() {
		try {
			Map<String,Object> parameters = new HashMap<String,Object>();
			Entity collection = entityService.getEntity(collectionId);
			setLastMessage("Finding aid export beginning...");
			ReportingDataSource data = new ReportingDataSource(getEntityList(collection), parameters);
			if(format.equals("pdf")) {				
				reportingService.generate(getUid(), false, ReportingService.FORMAT_PDF, title, "finding_aid.jasper", data);
			} else if(format.equals("rtf")) {				
				reportingService.generate(getUid(), false, ReportingService.FORMAT_RTF, title, "finding_aid.jasper", data);
			} else {
				reportingService.generate(getUid(), false, ReportingService.FORMAT_HTML, title, "finding_aid.jasper", data);
			}			
			setComplete(true);
		} catch(Exception e) {
			e.printStackTrace();
			setLastMessage(e.getLocalizedMessage());
			setComplete(true);
		}
	}
	
	protected List<Entity> getEntityList(Entity collection) throws InvalidQualifiedNameException, InvalidEntityException, InvalidPropertyException  {
		ContainerSorter containerSorter = new ContainerSorter(entityService);
		List<Entity> nodeMap = new ArrayList<Entity>();
		Entity e0 = populateReportFields(0, collection);
		nodeMap.add(e0);
		List<Association> level1Children = collection.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
		Collections.sort(level1Children, containerSorter);
		for(int i=0; i < level1Children.size(); i++) {
			Entity entity1 = entityService.getEntity(level1Children.get(i).getTarget());
			Entity e1 = populateReportFields(1, entity1);
			level1Children.get(i).setSourceEntity(entity1);			
			nodeMap.add(e1);
			List<Association> level2Children = entity1.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
			Collections.sort(level2Children, containerSorter);
			for(int j=0; j < level2Children.size(); j++) {
				Entity entity2 = entityService.getEntity(level2Children.get(j).getTarget());
				Entity e2 = populateReportFields(2, entity2);
				level2Children.get(j).setSourceEntity(entity2);
				nodeMap.add(e2);
				List<Association> level3Children = entity2.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
				Collections.sort(level3Children, containerSorter);
				for(int k=0; k < level3Children.size(); k++) {
					Entity entity3 = entityService.getEntity(level3Children.get(k).getTarget());
					Entity e3 = populateReportFields(3, entity3);
					level3Children.get(k).setSourceEntity(entity3);
					nodeMap.add(e3);
					List<Association> level4Children = entity3.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
					Collections.sort(level4Children, containerSorter);
					for(int l=0; l < level4Children.size(); l++) {
						Entity entity4 = entityService.getEntity(level4Children.get(l).getTarget());
						Entity e4 = populateReportFields(4, entity4);
						level4Children.get(l).setSourceEntity(entity4);
						nodeMap.add(e4);
						List<Association> level5Children = entity4.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
						Collections.sort(level5Children, containerSorter);
						for(int m=0; m < level5Children.size(); m++) {
							Entity entity5 = entityService.getEntity(level5Children.get(m).getTarget());
							Entity e5 = populateReportFields(5, entity5);
							level5Children.get(m).setSourceEntity(entity5);							
							nodeMap.add(e5);
							List<Association> level6Children = entity5.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
							Collections.sort(level6Children, containerSorter);
							for(int n=0; n < level6Children.size(); n++) {
								Entity entity6 = entityService.getEntity(level6Children.get(n).getTarget());
								Entity e6 = populateReportFields(6, entity6);
								level6Children.get(n).setSourceEntity(entity6);								
								nodeMap.add(e6);
							}
						}
					}
				}
			}
		}
		setLastMessage("Finding aid exporting "+nodeMap.size()+" items...");
		return nodeMap;
	}
	protected Entity populateReportFields(int level, Entity entity) {
		Entity e1 = new Entity();
		String name = entity.getName();
		try {
			e1.addProperty(new QName("", "level"), String.valueOf(level));
			if(level == 0) {
				e1.addProperty(new QName("", "name"), entity.getName());
				e1.addProperty(new QName("", "identifier"), "#"+HTMLUtility.removeTags(entity.getPropertyValue(RepositoryModel.COLLECTION_IDENTIFIER)));
				e1.addProperty(new QName("", "accession"), HTMLUtility.removeTags(entity.getPropertyValue(RepositoryModel.ACCESSION_DATE)));
			} else {
				String desc = entity.getPropertyValue(RepositoryModel.DESCRIPTION);
				String summary = entity.getPropertyValue(RepositoryModel.SUMMARY);
				if(summary != null && summary.length() > 0) summary = "["+summary+"]";
				else summary = "";
				String container1 = entity.getPropertyValue(RepositoryModel.CONTAINER);
				String[] containers1 = getContainers(container1);
				e1.addProperty(new QName("", "marker"), util.getNextMarker(level)+".");
				if(desc != null && desc.length() > 0) {
					if(summary != null && summary.length() > 0) e1.addProperty(new QName("", "description"), desc+" "+summary);
					else e1.addProperty(new QName("", "description"), desc);
				} else {
					if(summary != null && summary.length() > 0) e1.addProperty(new QName("", "description"), name+" "+summary);
					else e1.addProperty(new QName("", "description"), name);	
				}
				if(level > 2 && containers1.length > 0) e1.addProperty(new QName("", "container1"), containers1[0]);
				else e1.addProperty(new QName("", "container1"), "");
				if(containers1.length > 1) e1.addProperty(new QName("", "container2"), containers1[1]);
				else e1.addProperty(new QName("", "container2"), "");
			}
		} catch(InvalidPropertyException e) {
			e.printStackTrace();
		}
		return e1;
	}
	protected String[] getContainers(String container) {
		container = container.replace("  ", " ").trim();
		String[] values = {"",""};
		String[] containers = new String[6];
		if(container.contains(",")) {
			String[] parts = container.split(",");
			if(parts.length > 0) {
				String[] chunks = parts[0].split(" ");
				if(chunks.length == 2) {
					containers[0] = chunks[0];
					containers[1] = chunks[1];
				}
			}
			if(parts.length > 1) {
				String[] chunks = parts[1].trim().split(" ");
				if(chunks.length == 2) {
					containers[2] = chunks[0];
					containers[3] = chunks[1];
				}
			}
		} else {
			String[] parts = container.split(" ");
			if(parts.length > 0) containers[0] = parts[0];
			if(parts.length > 1) containers[1] = parts[1];
			if(parts.length > 2) containers[2] = parts[2];
			if(parts.length > 3) containers[3] = parts[3];
			if(parts.length > 4) containers[4] = parts[4];
			if(parts.length > 5) containers[5] = parts[5];
		}
		if(containers[0] != null && containers[1] != null) {
			if(containers[0].trim().toLowerCase().equals("folder")) values[1] += "[F. "+containers[1].trim()+"]";
			else if(containers[0].trim().toLowerCase().equals("package")) values[1] += "[P. "+containers[1].trim()+"]";
			else values[0] = containers[0].trim()+" "+ containers[1].trim();
		}
		if(containers[2] != null && containers[3] != null) {
			if(containers[2].trim().toLowerCase().equals("folder")) values[1] += "[F. "+containers[3].trim()+"]";
			if(containers[2].trim().toLowerCase().equals("package")) values[1] += "[P. "+containers[3].trim()+"]";
		}
		if(containers[4] != null && containers[5] != null) {
			if(containers[4].trim().toLowerCase().equals("folder")) values[1] += " [F. "+containers[5].trim()+"]";
			if(containers[4].trim().toLowerCase().equals("package")) values[1] += " [P. "+containers[5].trim()+"]";
		}
		if(values[0] != null) {
			if(values[0].length() == 0 || values[0].equals(util.getPrimaryContainer())) values[0] = "";
			else util.setPrimaryContainer(values[0]);
		} else values[0] = "";
		if(values[1] != null) {
			if(values[1].equals(util.getSecondaryContainer())) values[1] = "";
			else util.setSecondaryContainer(values[1]);
		} else values[1] = "";
		return values;
	}
	
	public EntityService getEntityService() {
		return entityService;
	}
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public ReportingService getReportingService() {
		return reportingService;
	}

	public void setReportingService(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
}
