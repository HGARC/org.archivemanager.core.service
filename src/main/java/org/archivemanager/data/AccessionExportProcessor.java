package org.archivemanager.data;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.heed.openapps.dictionary.ContactModel;
import org.heed.openapps.entity.Association;
import org.heed.openapps.entity.Entity;
import org.heed.openapps.entity.ExportProcessor;
import org.heed.openapps.entity.InvalidEntityException;
import org.heed.openapps.entity.data.FormatInstructions;

public class AccessionExportProcessor implements ExportProcessor {
	private static final long serialVersionUID = -7015183461999400108L;

	
	public String toXml(Association association, boolean printSource, boolean isTree) {
		StringWriter writer = new StringWriter();
		if(association.getQName().equals(ContactModel.ADDRESSES)) {			
			try {
				Entity accession = association.getSourceEntity();
				if(accession != null && accession.getId() != null) {
					writer.append("<parent>"+accession.getId()+"</parent>");
				}
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
		return writer.toString();
	}
	
	public final Map<String,Object> exportMap(FormatInstructions instructions, Association association) throws InvalidEntityException {
		Map<String,Object> data = new HashMap<String,Object>();
		if(association.getQName().equals(ContactModel.ADDRESSES)) {			
			try {
				Entity address = association.getTargetEntity();
				if(address != null && address.getId() != null) {
					List<Association> targets = address.getTargetAssociations(ContactModel.ADDRESSES);
					for(Association target : targets) {
						if(target.getSourceName().equals(ContactModel.INDIVIDUAL) || target.getSourceName().equals(ContactModel.ORGANIZATION)) {
							data.put("parent", target.getSource());
						}
					}					
				}
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
		return data;
	}
	@Override
	public Object export(FormatInstructions instructions, Entity entity) throws InvalidEntityException {
		if(instructions.getFormat().equals(FormatInstructions.FORMAT_JSON)) return new HashMap<String,Object>();
		else return "";
	}
	
	@Override
	public Object export(FormatInstructions instructions, Association association)	throws InvalidEntityException {
		if(instructions.getFormat().equals(FormatInstructions.FORMAT_JSON)) return exportMap(instructions, association);
		else return toXml(association, instructions.printSources(), instructions.printTargets());
		
	}
}
