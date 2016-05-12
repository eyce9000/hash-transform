package com.github.eyce9000.htl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.eyce9000.htl.RowTransform.Mode;
import com.github.eyce9000.htl.fields.CoalesceFunction;
import com.github.eyce9000.htl.fields.FieldBuilderFunction;
import com.github.eyce9000.htl.fields.FieldTransformFunction;

public class RowTransformFactory {
	private Map<String,List<String>> fields = new HashMap<>();
	private Map<String,Object> defaults = new HashMap<>();
	private Map<String,FieldTransformFunction> colTransforms = new HashMap<>();
	private Map<String,FieldBuilderFunction> colBuilders = new HashMap<>();
	private Mode mode;
	
	private List<Field> builders = new ArrayList<Field>();
	
	public RowTransformFactory(){
		this(Mode.Union);
	}
	
	public RowTransformFactory(Mode mode){
		this.mode = mode;
	}
	
	@Deprecated
	public Field field(String to){
		return to(to);
	}
	
	public Field to(String to){
		Field builder = new Field(this,to);
		builders.add(builder);
		return builder;
	}
	
	public RowTransformFactory putField(String oldFieldName,String newFieldName){
		List<String> newFieldNames;
		if(fields.containsKey(oldFieldName)){
			newFieldNames = fields.get(oldFieldName);
			newFieldNames.add(newFieldName);
		}
		else{
			newFieldNames = new LinkedList<String>();
			newFieldNames.add(newFieldName);
			fields.put(oldFieldName,newFieldNames);
		}
		return this;
	}
	
	public RowTransformFactory putField(String oldFieldName,String newFieldName,Object defaultValue){
		putField(oldFieldName,newFieldName);
		defaults.put(newFieldName,defaultValue);
		return this;
	}
	public RowTransformFactory putField(String oldFieldName,String newFieldName, FieldTransformFunction colTransform){
		putField(oldFieldName,newFieldName);
		colTransforms.put(newFieldName,colTransform);
		return this;
	}
	public RowTransformFactory putField(String oldFieldName,String newFieldName, FieldTransformFunction colTransform,Object defaultValue){
		putField(oldFieldName,newFieldName);
		colTransforms.put(newFieldName,colTransform);
		defaults.put(newFieldName,defaultValue);
		return this;
	}
	public RowTransformFactory putField(String newFieldName, FieldBuilderFunction colBuilder){
		colBuilders.put(newFieldName, colBuilder);
		return this;
	}
	public RowTransformFactory putField(String newFieldName, FieldBuilderFunction colBuilder,Object defaultValue){
		defaults.put(newFieldName,defaultValue);
		return putField(newFieldName,colBuilder);
	}
	public RowTransform build(){
		for(Field field: builders){
			field.apply();
		}
		RowTransform transform = new RowTransform();
		transform.setFieldNameMapping(fields);
		transform.setColumnTransforms(colTransforms);
		transform.setColumnBuilders(colBuilders);
		transform.setFieldDefaults(defaults);
		transform.setMode(mode);
		return transform;
	}
	
	public class Field{
		RowTransformFactory factory;
		String to;
		Field subField;
		
		private Field(){
		}

		Field(RowTransformFactory factory, String to){
			this.factory = factory;
			this.to = to;
		}
		
		public FieldTransform from(String from){
			if(this.subField!=null)
				throw new IllegalArgumentException("field is already defined");
			subField = new FieldTransform(this,from);
			return (FieldTransform)subField;
		}

		public void withDefault(Object def){
			if(this.subField!=null)
				throw new IllegalArgumentException("field is already defined");
			subField = new FieldTransform(this,to);
			subField.withDefault(def);
		}
		
		public FieldBuilder builder(FieldBuilderFunction builder){
			if(this.subField!=null)
				throw new IllegalArgumentException("field is already defined");
			subField = new FieldBuilder(this,builder);
			return (FieldBuilder) subField;
		}
		
		public FieldTransform transform(FieldTransformFunction transform){
			if(this.subField!=null)
				throw new IllegalArgumentException("field is already defined");
			subField = new FieldTransform(this,to).transform(transform);
			return (FieldTransform) subField;
		}
		
		public FieldBuilder coalesce(String... fieldNames){
			return builder(new CoalesceFunction(fieldNames));
		}
		
		public RowTransformFactory done(){
			return factory;
		}
		
		protected void apply(){
			if(subField==null)
				putField(to,to);
			else
				subField.apply();
		}
		

	}
	
	public class FieldTransform extends Field{
		
		String from;
		Object def;
		FieldTransformFunction transform;
		
		public FieldTransform(Field parent,String from){
			this.to = parent.to;
			this.factory = parent.factory;
			this.from = from;
		}
		
		@Override
		public void withDefault(Object def){
			if(this.def!=null)
				throw new IllegalArgumentException("default is already defined");
			this.def = def;
		}
		
		
		@Override
		public FieldTransform transform(FieldTransformFunction transform){
			if(this.transform!=null)
				throw new IllegalArgumentException("transform is already defined");
			this.transform = transform;
			return this;
		}

		@Override
		protected void apply(){
			if(transform!=null){
				if(def!=null)
					putField(from,to,transform,def);
				else
					putField(from,to,transform);
			}
			else if(def!=null){
				putField(from,to,def);
			}
			else{
				putField(from,to);
			}
		}
	}
	
	public class FieldBuilder extends Field{
		Object def;
		FieldBuilderFunction builder;
		FieldBuilder(Field parent,FieldBuilderFunction builder) {
			this.to = parent.to;
			this.factory = parent.factory;
			this.builder = builder;
		}

		@Override
		public void withDefault(Object value){
			if(this.def!=null)
				throw new IllegalArgumentException("default is already defined");
			this.def = value;
		}

		@Override
		protected void apply(){
			if(def==null){
				putField(to,builder);
			}
			else{
				putField(to,builder,def);
			}
		}
	}
}
