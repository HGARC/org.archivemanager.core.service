package org.archivemanager.util;

import java.util.ArrayList;
import java.util.List;

import org.archivemanager.model.Collection;
import org.archivemanager.model.Series;
import org.json.JSONObject;


public class CollectionSeriesJsonSerializer {
	//private static Log log = LogFactoryUtil.getLog(CollectionSeriesJsonSerializer.class);
	
	public static JSONObject toJSONObject(int status, Collection model) {
	    JSONObject jsonObj = new JSONObject();
	    put(jsonObj, "id", model.getId());
	    put(jsonObj, "name", model.getTitle());
	    List<JSONObject> seriesList = new ArrayList<JSONObject>();
	    for(Series series : model.getSeries()) {
	    	JSONObject seriesObj = new JSONObject();
	    	put(seriesObj, "id", series.getId());
	    	put(seriesObj, "name", series.getTitle());
	    	put(seriesObj, "description", series.getDescription());
	    	seriesList.add(seriesObj);
	    }
	    put(jsonObj, "series", seriesList);
	    return jsonObj;
	}
	
	protected static void put(JSONObject jsonObj, String key, Object value) {
		try {
			jsonObj.put(key, value);
		} catch(Exception e) {
			//log.error("", e);
		}
	}
}
