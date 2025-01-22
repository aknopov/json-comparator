package com.aknopov.jsoncompare;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonComparatorTest
{
    private final static String EMPTY_SAMPLE = "{}";
    private final static String JSON_SAMPLE_1 =
"""
{"a": {"b": "foo", "c": 5, "d": {"e": "bar"}, "f": [13, 17, 31]}}
""";
    private final static String JSON_SAMPLE_2 =
"""
{"a": {"b": "foo", "c": 5, "d": {"e": "foo"}, "f": [13, 17, 31]}}
""";
    private final static String JSON_SAMPLE_3 =
"""
{"a": {"b": "bar", "c": 5, "d": {"e": "foo"}, "f": [13, 15]}}
""";
    private final static String JSON_SAMPLE_4 =
"""
{"a": {"b": "foo", "c": 5}, "d": "org"}
""";
private final static String JSON_SAMPLE_5 =
"""
{"a": {"c": 5, "b": "foo"}, "d": "mod"}
""";
    private final static String INVALID_JSON_SAMPLE =
"""
{"a": {"b": "foo", "c": 5, "d": {"e": "bar"}}
""";
    private final static String JSON_ARRAY_1 = """
[
  {
     "id": 1,
     "name": "a",
     "passed": true
  },
  {
     "id": 2,
     "name": "b",
     "passed": true
  }
]
""";
    private final static String JSON_ARRAY_2 = """
[
  {
     "id": 1,
     "name": "a",
     "passed": true
  },
  {
     "id": 2,
     "name": "c",
     "passed": false
  }
]
""";
    private final static String JSON_ARRAY_3 = "[1.0, 2.0, 3.0, 4.0]";
    private final static String JSON_ARRAY_4 = "[1.0, 3.0, 5.0, 4.0]";

    @Test
    void testParsingInvalidSamples()
    {
        assertEquals(List.of("Empty input for the first sample"), JsonComparator.compareJsonStrings("", "", true));
        assertEquals(List.of("Empty input for the second sample"), JsonComparator.compareJsonStrings(JSON_SAMPLE_1, "", true));

        List<String> firstErr = JsonComparator.compareJsonStrings("not a JSON", "", true);
        assertTrue(!firstErr.isEmpty() && firstErr.get(0).startsWith("Failed to parse the first sample:"));

        List<String> secondErr = JsonComparator.compareJsonStrings(JSON_SAMPLE_1, "not a JSON", true);
        assertTrue(!secondErr.isEmpty() && secondErr.get(0).startsWith("Failed to parse the second sample:"));

        List<String> thirdErr = JsonComparator.compareJsonStrings(INVALID_JSON_SAMPLE, JSON_SAMPLE_1, true);
        assertTrue(!thirdErr.isEmpty() && thirdErr.get(0).startsWith("Failed to parse the first sample:"));
    }

    @Test
    void testIdentityEquivalence()
    {
        assertTrue(JsonComparator.compareJsonStrings(JSON_SAMPLE_1, JSON_SAMPLE_1, false).isEmpty());

        assertTrue(JsonComparator.compareJsonStrings(EMPTY_SAMPLE, EMPTY_SAMPLE, false).isEmpty());
    }

    @Test
    void testArrayComparison()
    {
        List<String> diffs = JsonComparator.compareJsonStrings(JSON_ARRAY_1, JSON_ARRAY_2, false);
        assertEquals(List.of("Nodes values differ: 'b' vs 'c', path='/[1]/name[1]'",
                "Nodes values differ: 'true' vs 'false', path='/[1]/passed[2]'"), diffs);
    }

    private static Stream<Arguments> comparisonSource()
    {
        return Stream.of(
            Arguments.of(JSON_SAMPLE_1, EMPTY_SAMPLE, List.of("Children differ: counts 1 vs 0 (diffs: a[0-0]:+1), path='/'")),
            Arguments.of(EMPTY_SAMPLE, JSON_SAMPLE_1, List.of("Children differ: counts 0 vs 1 (diffs: a[0-0]:-1), path='/'")),
            Arguments.of(JSON_SAMPLE_1, JSON_SAMPLE_2, List.of("Nodes values differ: 'bar' vs 'foo', path='/a/d[2]/e'")),
            Arguments.of(JSON_SAMPLE_1, JSON_SAMPLE_3, List.of("Nodes values differ: 'foo' vs 'bar', path='/a/b[0]'",
                    "Nodes values differ: 'bar' vs 'foo', path='/a/d[2]/e'",
                    "Children differ: counts 3 vs 2 (diffs: [1-1]:+1), path='/a/f[3]'")),
            Arguments.of(JSON_ARRAY_3, JSON_ARRAY_4, List.of("Nodes values differ: '2.0' vs '3.0', path='/[1]'",
                    "Nodes values differ: '3.0' vs '5.0', path='/[2]'"))
        );
    }

    @ParameterizedTest
    @MethodSource("comparisonSource")
    void testComparison(String sample1, String sample2, List<String> expectedDiffs)
    {
        List<String> actualDiffs = JsonComparator.compareJsonStrings(sample1, sample2, false);

        assertEquals(expectedDiffs, actualDiffs);
    }

    @Test
    void testStopOnFirst()
    {
        List<String> diffs = JsonComparator.compareJsonStrings(JSON_SAMPLE_1, JSON_SAMPLE_3, true);
        assertEquals(1, diffs.size());
    }

    @Test
    void testIgnoringKnownDiscrepancies()
    {
        List<String> diffs = JsonComparator.compareJsonStrings(JSON_SAMPLE_4, JSON_SAMPLE_5, false);
        assertEquals(List.of("Children order differ for 2 nodes, path='/a[0]'",
                "Nodes values differ: 'org' vs 'mod', path='/d[1]'"), diffs);

        diffs = JsonComparator.compareJsonStrings(JSON_SAMPLE_4, JSON_SAMPLE_5, false,
                List.of("Children order differ for \\d+ nodes, path='/a.*'"));
        assertEquals(List.of("Nodes values differ: 'org' vs 'mod', path='/d[1]'"), diffs);
    }
}