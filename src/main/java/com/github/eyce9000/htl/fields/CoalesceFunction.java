package com.github.eyce9000.htl.fields;

import java.util.Map;

public class CoalesceFunction implements FieldBuilderFunction {
	private String[] fieldNames;

	public CoalesceFunction(String... fieldNames){
		this.fieldNames = fieldNames;
	}
	
	@Override
	public Object apply(Map<String, Object> arg0) {
		Object value = null;
		for(String fieldName:fieldNames){
			value = arg0.get(fieldName);
			if(value!=null)
				return value;
		}
		return value;
	}

}
