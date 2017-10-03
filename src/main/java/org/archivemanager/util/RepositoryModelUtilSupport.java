package org.archivemanager.util;

public abstract class RepositoryModelUtilSupport {

	
	public static String cleanContentType(String in) {
		if(in == null) return "";
		if(in.equals("audio")) return "Audio";
		if(in.equals("correspondence")) return "Correspondence";
		if(in.equals("financial")) return "Financial Material";
		if(in.equals("legal")) return "Legal Material";
		if(in.equals("manuscript")) return "Manuscript";
		if(in.equals("memorabilia")) return "Memorabilia";
		if(in.equals("photographs")) return "Photographic Material";
		if(in.equals("printed_material")) return "Printed Material";
		if(in.equals("professional")) return "Professional Material";
		if(in.equals("video")) return "Video";
		if(in.equals("research")) return "Research";
		return in;
	}
}
