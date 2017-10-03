package org.archivemanager.util;

import org.heed.openapps.util.NumberUtility;

public class FindingAidUtil {
	private int level1_idx = 0;
	private int level2_idx = 0;
	private int level3_idx = 0;
	private int level4_idx = 0;
	private int level5_idx = 0;
	
	private String primaryContainer = "";
	private String secondaryContainer = "";
	
	private String[] level2Mark = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	private String[] level3Mark = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
	
	public void reset() {
		level1_idx = 0;
		level2_idx = 0;
		level3_idx = 0;
		level4_idx = 0;
		level5_idx = 0;
	}
	public String getNextMarker(int level) {
		String marker = null;
		if(level == 1) {
			marker = NumberUtility.int2RomanNumeral(level1_idx+1);
			level2_idx = 0;
			level1_idx++;
		} else if(level == 2) {
			int mult = (int)Math.floor(level2_idx / 26);
			int index = level2_idx - (26 * mult);	
			marker = "";
			for(int q=0; q <= mult; q++) marker += level2Mark[index];
			level3_idx = 0;
			level2_idx++;
		} else if(level == 3) {
			marker = String.valueOf(level3_idx+1);
			level4_idx = 0;
			level3_idx++;
		} else if(level == 4) {
			int mult = (int)Math.floor(level4_idx / 26);
			int index = level4_idx - (26 * mult);			
			marker = "";
			for(int q=0; q <= mult; q++) marker += level3Mark[index];
			level5_idx = 0;
			level4_idx++;
		} else if(level == 5) {
			marker = NumberUtility.int2RomanNumeral(level5_idx+1).toLowerCase();
			level5_idx++;
		}
		return marker;
	}
	public String getPrimaryContainer() {
		return primaryContainer;
	}
	public void setPrimaryContainer(String primaryContainer) {
		this.primaryContainer = primaryContainer;
	}
	public String getSecondaryContainer() {
		return secondaryContainer;
	}
	public void setSecondaryContainer(String secondaryContainer) {
		this.secondaryContainer = secondaryContainer;
	}
	
}
