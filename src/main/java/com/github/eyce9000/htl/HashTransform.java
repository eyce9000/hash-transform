package com.github.eyce9000.htl;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.github.eyce9000.htl.RowTransform.Mode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;


public class HashTransform implements Transform{
	List<Closure> runAfter = new ArrayList<Closure>();
	List<Transform> transforms = new ArrayList<Transform>();
	
	public void mapper(Closure c){
		factory(c,new RowTransformFactory(Mode.NewOnly));
	}
	
	public void union(Closure c){
		factory(c,new RowTransformFactory(Mode.Union));
	}
	
	public void intersection(Closure c){
		factory(c,new RowTransformFactory(Mode.Intersection));
	}
	
	public void exclude(String...fields){
		transforms.add(new FilterTransform(FilterTransform.Type.Exclude,fields));
	}
	public void include(String...fields){
		transforms.add(new FilterTransform(FilterTransform.Type.Include,fields));
	}
	
	public void transform(Transform t){
		transforms.add(t);
	}
	
	
	private void factory(Closure c, RowTransformFactory factory){
		c.setDelegate(factory);
		c.setResolveStrategy(Closure.DELEGATE_FIRST);
		c.call();
		addMapper(factory.build());
	}
	
	private void addMapper(RowTransform transform){
		transforms.add(transform);
	}
	
	public void action(Closure c){
		this.runAfter.add(c);
	}
	
	@Override
	public Map<String,Object> transform(Map<String,Object> inputRaw){
		Map<String,Object> output = new HashMap<>(inputRaw);
		
		for(Transform mapper:transforms){
			 output = new HashMap<>(mapper.transform(output));
		}
		
		for(Closure c : runAfter){
			c.setDelegate(output);
			c.call(output);
		}
		return output;
	}
	
	public static HashTransform createTransform(String src){
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(DelegatingScript.class.getCanonicalName());
		GroovyShell shell = new GroovyShell(HashTransform.class.getClassLoader(), new Binding(), configuration);
		DelegatingScript delegatingScript = (DelegatingScript)shell.parse(src);
		HashTransform transform = new HashTransform();
		delegatingScript.setDelegate(transform);
		delegatingScript.run();
		return transform;
	}
	public static HashTransform createTransform(File scriptFile) throws CompilationFailedException, IOException{
		CompilerConfiguration configuration = new CompilerConfiguration();
		configuration.setScriptBaseClass(DelegatingScript.class.getCanonicalName());
		GroovyShell shell = new GroovyShell(HashTransform.class.getClassLoader(), new Binding(), configuration);
		DelegatingScript delegatingScript = (DelegatingScript)shell.parse(scriptFile);
		HashTransform transform = new HashTransform();
		delegatingScript.setDelegate(transform);
		delegatingScript.run();
		return transform;
	}
}
