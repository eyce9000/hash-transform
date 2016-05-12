# Hash Transform Library for Java #
This library provides tools for transforming hash maps to new hash maps or objects. This is ideal for:

* Transforming between schemas
* Processing streams of JSON objects
* Processing streams of tabular data

## Using the Library ##
To use Hash Transform (htl) you can either use the fluent Java API, or a Groovy-based DSL file, to create a transformer object. You then feed Map<String,Object> objects into the transform and it will clone the map and transform it.

## Examples ##

### User Transform ###

#### Source Record ####
```json
{
  "NAME":"George Lucchese",
  "PHONE":"555-559-9222",
  "EMAIL":"george@lucchese.com"
}
```

#### Target Record ####
```json
{
  "firstName":"George",
  "lastName": "Lucchese",
  "phone":"555-559-9222",
  "email":"george@lucchese.com",
  "type": "admin"
}
```

#### The HTL Groovy DSL File ####
Define the transform in groovy:
```groovy
//UserTransform.groovy
mapper {
  to "phoneNumber" from "PHONE"
  to "firstName" from "NAME" transform {it.split(" ").getAt(0)}
  to "lastName" from "NAME" transform {it.split(" ").getAt(1)}
  to "email" from "EMAIL"
  to "type" withDefault "admin"
}

```
Load and use the transform in java:
```java
HashTransform transform = HashTransform.createTransform(new File("UserTransform.groovy"))

Map<String,Object> raw = new Map<String,Object>()
raw.put("NAME","George Lucchese")
raw.put("PHONE", "555-559-9222")
raw.put("EMAIL", "george@lucchese.com")

System.out.println(transform.transform(raw))
```
