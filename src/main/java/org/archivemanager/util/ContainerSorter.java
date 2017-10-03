package org.archivemanager.util;
import java.util.Comparator;
import java.util.List;

import org.heed.openapps.dictionary.RepositoryModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.EntityService;
import org.heed.openapps.util.NumberUtility;


public class ContainerSorter implements Comparator<Association> {
	private EntityService entityService;
	
	public ContainerSorter(EntityService entityService) {
		this.entityService = entityService;
	}
	
	public int compare(Association assoc1, Association assoc2) {
		try {
			Entity doc1 = entityService.getEntity(assoc1.getTarget());
			Entity doc2 = entityService.getEntity(assoc2.getTarget());
			String field1 = doc1.getPropertyValue(RepositoryModel.CONTAINER);
			String field2 = doc2.getPropertyValue(RepositoryModel.CONTAINER);
			if(field1 != null && (field2 != null)) {
				if(field1.equals(field2)) return 0;
				if(!field1.equals("") && !field2.equals("")) {
					String[] parts1 = field1.trim().replace("  ", " ").split(" ");
					String[] parts2 = field2.trim().replace("  ", " ").split(" ");
					if(parts1.length > 1 && parts2.length > 1) {
						int vote1 = compareContainer(parts1[0].toLowerCase().trim(), parts1[1].replace(",","").trim(), parts2[0].toLowerCase().trim(), parts2[1].replace(",","").trim());
						if(vote1 != 0) return vote1;
						if(parts1.length > 3 && parts2.length > 3) {
							int vote2 = compareContainer(parts1[2].toLowerCase(), parts1[3].replace(",",""), parts2[2].toLowerCase(), parts2[3].replace(",",""));
							if(vote2 != 0) return vote2;
							if(parts1.length > 5 && parts2.length > 5) {
								int vote3 = compareContainer(parts1[4].toLowerCase(), parts1[5].replace(",",""), parts2[4].toLowerCase(), parts2[5].replace(",",""));
								if(vote3 != 0) return vote3;
							} else {
								if(parts2.length > 5) return 1;
								if(parts2.length > 5) return -1;
							}
						} else {
							if(parts2.length > 3) return 1;
							if(parts2.length > 3) return -1;
						}
					} else {
						if(parts1.length > 1) return 1;
						if(parts2.length > 1) return -1;
					}
				} else if((field1 != null && !field1.equals(""))) 
					return -1;
				else if((field2 != null && !field2.equals(""))) 
					return 1;
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;		
	}
	
	protected int sortByChildren(Entity e1, Entity e2) {
		List<Association> e1Children = e1.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
		List<Association> e2Children = e2.getSourceAssociations(RepositoryModel.CATEGORIES, RepositoryModel.ITEMS);
		if(e1Children.size() == 0 && e2Children.size() > 0) return -1;
		if(e1Children.size() > 0 && e2Children.size() == 0) return 1;
		if(e1Children.size() == 0 && e2Children.size() == 0) return 0;
		try {
			Association a1 = e1Children.get(0);
			Association a2 = e2Children.get(0);
			a1.setTargetEntity(entityService.getEntity(a1.getTarget()));
			a2.setTargetEntity(entityService.getEntity(a2.getTarget()));
			return compare(a1, a2);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	protected int compareContainer(String type1, String value1, String type2, String value2) {
		if(type1.equals("boxes")) type1 = "box";
		if(type2.equals("boxes")) type2 = "box";
		if(type1.equals(type2) && value1.equals(value2)) return 0;
		if(type1.equals(type2)) return compareNumericStrings(value1, value2);
		if(type1.startsWith("box")) return 1;
		if(type1.startsWith("reels")) {
			if(type2.startsWith("box")) return -1;
			else return 1;
		} else if(type1.startsWith("package")) {
			if(type2.startsWith("box") || type2.startsWith("reels")) return -1;
			else return 1;
		} else if(type1.startsWith("folder")) {
			if(type2.startsWith("box") || type2.startsWith("reels") || type2.startsWith("package")) return -1;
			else return 1;
		} else if(type1.equals("film") && value1.equals("Vault")) return 1; 
		return compareNumericStrings(value1, value2);
	}
	protected int compareNumericStrings(String field1, String field2) {
		if(field1.equals(field2)) return 0;
		String[] parts1 = field1.split("-");
		String[] parts2 = field2.split("-");
		if(parts1.length > 0 && parts2.length > 0) {
			if(parts1[0] != parts2[0] && NumberUtility.isInteger(parts1[0]) && NumberUtility.isInteger(parts2[0]))
				return Integer.valueOf(parts1[0]).compareTo(Integer.valueOf(parts2[0]));
			for(int i=0; i < 5; i++) {
				if(parts1[0].length() > 0) {
					if(parts2[0].length() > 0) {
						if(parts1[0].charAt(0) != parts2[0].charAt(0)) {
							if(parts1[0].charAt(0) > parts2[0].charAt(0)) return 1;
							else if(parts1[0].charAt(0) < parts2[0].charAt(0)) return -1;
						}
					} else return 1;
					if(parts1[0].length() > 1) {
						if(parts2[0].length() > 1) {
							if(parts1[0].charAt(1) != parts2[0].charAt(1)) {
								if(parts1[0].charAt(1) > parts2[0].charAt(1)) return 1;
								else if(parts1[0].charAt(1) < parts2[0].charAt(1)) return -1;
							}
						} else return 1;
						if(parts1[0].length() > 2) {
							if(parts2[0].length() > 2) {
								if(parts1[0].charAt(2) != parts2[0].charAt(2)) {
									if(parts1[0].charAt(2) > parts2[0].charAt(2)) return 1;
									else if(parts1[0].charAt(2) < parts2[0].charAt(2)) return -1;
								}
							} else return 1;
							
						} else if(parts2[0].length() > 2) return -1;
					} else if(parts2[0].length() > 1) return -1;
				} else if(parts2[0].length() > 0) return -1;
			}
			/*
			if(NumberUtility.isInteger(parts1[0]) || (parts1[0].length() < parts2[0].length())) 
				return 1;
			if(NumberUtility.isInteger(parts2[0]) || (parts1[0].length() > parts2[0].length())) 
				return -1;
			if(parts1.length > parts2.length) 
				return -1;
			if(parts2.length > parts1.length) 
				return 1;
			if(parts1.length > 1 && parts2.length > 1) {
				if(parts1[1] != parts2[1] && NumberUtility.isInteger(parts1[1]) && NumberUtility.isInteger(parts2[1]))
					return Integer.valueOf(parts1[1]).compareTo(Integer.valueOf(parts2[1]));				
			}
			if(NumberUtility.isInteger(parts1[0])) 
				return 1;
			if(NumberUtility.isInteger(parts2[0])) 
				return -1;
			*/
		}
		return field1.compareTo(field2);
	}
}