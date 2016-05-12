package com.github.eyce9000.htl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.eyce9000.htl.fields.FieldBuilderFunction;
import com.github.eyce9000.htl.fields.FieldTransformFunction;

import java.util.Set;

public class RowTransform implements Transform{
	public enum Mode {Union,Intersection,NewOnly};
	
	private Map<String,List<String>> newFieldsByOldFieldName = new HashMap<String,List<String>>();
	private Map<String,Object> defaults = new HashMap<String,Object>();
	private Map<String,FieldTransformFunction> transformsByNewFieldName = new HashMap<String,FieldTransformFunction>();
	private Map<String,FieldBuilderFunction> buildersByNewFieldName = new HashMap<String,FieldBuilderFunction>();
	private Set<String> excludedFields = new HashSet<>();
	private Mode mode = Mode.NewOnly;
	RowTransform(){}
	
	void setFieldNameMapping(Map<String,List<String>> fields){
		this.newFieldsByOldFieldName.putAll(fields);
	}
	
	void setColumnTransforms(Map<String,FieldTransformFunction> colTransforms){
		this.transformsByNewFieldName.putAll(colTransforms);
	}
	void setFieldDefaults(Map<String,Object> defaults){
		this.defaults.putAll(defaults);
	}
	void setColumnBuilders(Map<String,FieldBuilderFunction> colBuilders){
		this.buildersByNewFieldName.putAll(colBuilders);
	}
	void setMode(Mode mode){
		this.mode = mode;
	}
	
	@Override
	public Map<String,Object> transform(Map<String,Object> raw){
		HashSet<String> unProcessedKeys = new HashSet<String>();
		for(List<String> fieldList:newFieldsByOldFieldName.values()){
			unProcessedKeys.addAll(fieldList);
		}
		unProcessedKeys.addAll(buildersByNewFieldName.keySet());
		
		Map<String,Object> processed = new HashMap<>();
		
		for(Entry<String,Object> rawEntry:raw.entrySet()){
			String key = rawEntry.getKey();
			Object value = rawEntry.getValue();
			
			if(newFieldsByOldFieldName.containsKey(key)){
				for(String newKey:newFieldsByOldFieldName.get(key)){
					processed.put(newKey,processValue(newKey,value,raw));
					unProcessedKeys.remove(newKey);
				}
			}
			else if(mode==Mode.Union){
				processed.put(key,value);
			}
			else{
				//Do nothing
			}
		}
		if(mode==Mode.Union || mode==Mode.NewOnly){
			for(String key:unProcessedKeys){
				Object value = null;
				value = processValue(key,value,raw);
				if(value!=null)
					processed.put(key,value);
			}
		}
		return processed;
	}
	
	private Object processValue(String newKey,Object value,Map<String,Object> raw){
		if(buildersByNewFieldName.containsKey(newKey)){
			value = buildersByNewFieldName.get(newKey).apply(raw);
		}
		else if(transformsByNewFieldName.containsKey(newKey)){
			value = transformsByNewFieldName.get(newKey).apply(value);
		}

		if(value==null && defaults.containsKey(newKey)){
			value = defaults.get(newKey);
		}
		return value;
	}
}
