package com.aknopov.jsoncompare;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonComparatorTest
{
    private final static String JSON_SAMPLE =
"""
{"a": {"b": "foo", "c": 5, "d": {"e": "bar"}, "f": [13, 17, 31]}}
""";
    private final static String INVALID_JSON_SAMPLE =
"""
{"a": {"b": "foo", "c": 5, "d": {"e": "bar"}}
""";
    @SuppressWarnings("UnusedVariable") //UC
    private final static String EMPTY_SAMPLE = "{}";
    private final static String ARRAY_1 = """
[
  {
     "id": 1,
     "name": "a"
  },
  {
     "id": 2,
     "name": "b"
  }
]
""";
    private final static String ARRAY_2 = """
[
  {
     "id": 1,
     "name": "a"
  },
  {
     "id": 2,
     "name": "c"
  }
]
""";

    @Test
    void testParsingInvalidSamples()
    {
        assertEquals(List.of("Empty input for the first sample"), JsonComparator.compareJsonStrings("", "", true));
        assertEquals(List.of("Empty input for the second sample"), JsonComparator.compareJsonStrings(JSON_SAMPLE, "", true));

        List<String> firstErr = JsonComparator.compareJsonStrings("not a JSON", "", true);
        assertTrue(!firstErr.isEmpty() && firstErr.get(0).startsWith("Failed to parse the first sample:"));

        List<String> secondErr = JsonComparator.compareJsonStrings(JSON_SAMPLE, "not a JSON", true);
        assertTrue(!secondErr.isEmpty() && secondErr.get(0).startsWith("Failed to parse the second sample:"));

        List<String> thirdErr = JsonComparator.compareJsonStrings(INVALID_JSON_SAMPLE, JSON_SAMPLE, true);
        assertTrue(!thirdErr.isEmpty() && thirdErr.get(0).startsWith("Failed to parse the first sample:"));
    }

    @Test
    void testIdentityEquivalence()
    {
        List<String> diffs = JsonComparator.compareJsonStrings(JSON_SAMPLE, JSON_SAMPLE, false);

        assertTrue(diffs.isEmpty());
    }

    @Test
    void testCompareArrays()
    {
        List<String> diffs = JsonComparator.compareJsonStrings(ARRAY_1, ARRAY_2, false);

        assertFalse(diffs.isEmpty()); //UC
    }
}