# JSON comparator 

Java library for comparing JSON strings

## Overview

API consists of two functions -
```
List<String> JsonComparator.compareJsonStrings(sample1 string, sample2 string, stopOnFirst bool);
List<String> JsonComparator.compareJsonStrings(sample1 string, sample2 string, stopOnFirst bool, List<String> ignored)
```

That return a list of detected differ**ences between two samples. Comparison can be stopped on the first occasion - `stopOnFirst=true`.
The second form takes a list of RegEx strings to be used as a filter for ignored differences.

Each entry in the returned list contains the JSON path to the node like  `..., path='/note/to[0]'`.
Path elements might contain zero-based index of an element in the siblings list.

When a difference in children elements is detected, the message has the form `Children differ: counts 3 vs 4 (diffs: ...)`
where the first number is the count of children in the first sample. Mismatched child elements in the `diffs` list have two numbers.
The first, in square brackets, is the index in the sibling nodes list.The second - suffix like `:+1` or `:-3` is the count
of consecutive mismatched elements with the same name. A positive number relates to the count of elements in `sample1`, negative - to `sample2`.
There is a difference in output of mismatched children between objects and arrays - object differences contains child names,
whereas arrays do not, but they range of indices of consecutive mismatched nodes.

Example of use as jUnit test - 
```java
import "com.aknopov.jsoncompare.JsonComparator";
import "org.junit.jupiter.*";
...
    String sample1 = """
    {"a": {"b": "foo", "c": 5, "d": {"e": "bar"}, "f": [13, 17, 31]}}
    """;
    String sample2 = """
    {"a": {"b": "bar", "c": 5, "d": {"e": "foo"}, "f": [13, 15]}}
    """;
    
    List<string> diffs = JsonComparator.compareJsonStrings(sample1, sample2, false);
    assertEquals("Nodes values differ: 'foo' vs 'bar', path='/a/b[0]'", diffs.get(0));
    assertEquals("Nodes values differ: 'bar' vs 'foo', path='/a/d[2]/e'", diffs.get(1));
    assertEquals("Children differ: counts 3 vs 2 (diffs: [1-1]:+1), path='/a/f[3]'", diffs.get(2));
```
