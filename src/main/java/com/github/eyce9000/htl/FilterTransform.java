package com.github.eyce9000.htl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterTransform implements Transform{
	public static enum Type{Include,Exclude}

	private Type type;
	
	private Set<String> fields = new HashSet<>();
	
	
	public FilterTransform(){
		this(Type.Exclude);
	}
	
	public FilterTransform(Type type,String... field){
		this.type = type;
		this.fields.addAll(Arrays.asList(field));
	}
	
	public void addField(String field){
		fields.add(field);
	}
	
	public void addFields(String... field){
		fields.addAll(Arrays.asList(field));
	}
	
	@Override
	public Map<String, Object> transform(Map<String, Object> raw) {
		Map<String,Object> processed = new HashMap<>(raw);
		switch(type){
			case Exclude:{
				for(String field:fields){
					processed.remove(field);
				}
				break;
			}
			case Include:{
				List<String> toRemove = new ArrayList<String>(processed.size());
				for(String key:processed.keySet()){
					if(!fields.contains(key))
						toRemove.add(key);
				}
				toRemove.stream().forEach(processed::remove);
			}
		}
		return processed;
		
	}

	public static class Factory{
		
	}
}
