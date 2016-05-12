package com.grl.htl



import static org.junit.Assert.*

import com.github.eyce9000.htl.HashTransform;
import com.github.eyce9000.htl.RowTransform;
import com.github.eyce9000.htl.RowTransformFactory;
import com.github.eyce9000.htl.RowTransform.Mode
import com.github.eyce9000.htl.fields.FieldBuilderFunction;
import com.github.eyce9000.htl.fields.FieldTransformFunction;

import org.junit.*

class RowTransformTest {
	Map<String,String> raw;
	@Before
	public void setup(){
		raw = ["key1":"value1","key2":"value2","key3":"value3"];
	}
	
	@Test
	public void testUnion(){
		RowTransformFactory builder = new RowTransformFactory(Mode.Union)
		builder
				.putField("key1", "new-key1")
				
				.putField("key1","new-key1b", {String value ->
					return "transformed-"+value;
				} as FieldTransformFunction)
				
				.putField("key4", "new-key4", "default-value4")
				
				.putField("key2", "new-key2", {String value ->
					return "transformed-"+value;
				} as FieldTransformFunction)
				
				.putField("new-key5", {Map<String,String> raw ->
					return raw.get("key1")+"-"+raw.get("key2");
				} as FieldBuilderFunction)
				
		RowTransform transform = builder.build()
		
		Map<String,String> processed = transform.transform(raw)
		
		Map<String,String> correctResult = [
			"new-key1":"value1",
			"new-key1b":"transformed-value1",
			"new-key2":"transformed-value2",
			"key3":"value3",
			"new-key4":"default-value4",
			"new-key5":"value1-value2"
			]
		
		assertEquals(correctResult,processed)
	}
	
	@Test
	public void testIntersection(){
		RowTransformFactory builder = new RowTransformFactory(Mode.Intersection)
		builder
			.putField("key1","new-key1")
			.putField("key1","new-key1b")
			.putField("key3", "new-key3", {String value->
				return "transformed-"+value
			}as FieldTransformFunction)
			.putField("new-key5", {Map<String,String> raw ->
				return raw.get("key1")+"-"+raw.get("key2");
			} as FieldBuilderFunction);
		
		RowTransform transform = builder.build()
		
		Map<String,String> processed = transform.transform(raw)
		
		Map<String,String> correctResult = [
			"new-key1":"value1",
			"new-key1b":"value1",
			"new-key3":"transformed-value3"
			]
		
		assertEquals(correctResult,processed)
		
	}
	@Test
	public void testStandardMapper(){
		HashTransform transform = HashTransform.createTransform("""
		mapper {
		field "key4" withDefault ""
		field "key1"
		field "key6" transform {return null}
		field "key5" transform {return null} withDefault "test2"
		}
			""")
		
		Map<String,String> processed = transform.transform(raw)
		
		Map<String,String> correctResult = [
			"key1":"value1",
			"key4":"",
			"key5":"test2"
			]
		
		assertEquals(correctResult,processed)
	}
	
	@Test
	public void testCompiled(){
		HashTransform transform = HashTransform.createTransform("""
mapper { 
	to "new_key1" from "key1" 
	to "new_key2" from "key2" transform {
		it.toLowerCase()
	}
	to "new_key3" from "key3"
}

mapper {
	to "new_key1"
	to "key1AndKey2" builder {
		it["new_key1"]+"."+it["new_key2"]
	}
	to "new_key3" withDefault "value3"
}

include ("new_key1", "key1AndKey2")

union {
	to "new_key4" withDefault "value4"
}

exclude ("new_key4")

union {
	to "new_key1" transform {it.toUpperCase()}
	to "key6" withDefault "value6"
	to "key7" coalesce "key3","new_key1"
	to "key8" coalesce "key3" withDefault "value8"
}
		""");
		Map<String,String> processed = transform.transform(raw)
		
		Map<String,String> correctResult = [
			"new_key1":"VALUE1",
			"key1AndKey2":"value1.value2",
			"key6":"value6",
			"key7":"value1",
			"key8":"value8"
			]
		assertEquals(correctResult,processed)
	}
}