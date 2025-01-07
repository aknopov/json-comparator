package com.aknopov.jsoncompare;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test //UC
    void testParsing() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(JSON_SAMPLE);

        assertFalse(root.isEmpty());
        assertFalse(root.isValueNode());

        Iterator<String> itNames = root.fieldNames();
        while (itNames.hasNext()) {
            String key = itNames.next();
            System.err.printf("%s%n", key);//UC
        }

        printTree(root, 0);
    }

    private void printTree(JsonNode node, int level)
    {
        String margin = "  ".repeat(level);

        Iterator<Map.Entry<String, JsonNode>> itFields = node.fields();
        while (itFields.hasNext()) {
            Map.Entry<String, JsonNode> entry = itFields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isValueNode())
            {
                System.err.printf("%s%s = %s%n", margin, key, value);
            }
            else if (value.isArray())
            {
                System.err.printf("%s%s = [%n%s", margin, key, margin + "  ");
                Iterator<JsonNode> itArray = value.elements();
                while(itArray.hasNext())
                {
                    JsonNode arrayVal = itArray.next();
                    if (arrayVal.isObject())
                    {
                        printTree(arrayVal, level+1);
                    }
                    else
                    {
                        System.err.printf("%s, ", arrayVal);
                    }
                }
                System.err.printf("%n%s]%n", margin);
            }
            else
            {
                System.err.printf("%s%s = {%n", margin, key);
                printTree(value, level + 1);
                System.err.printf("%s}%n", margin);
            }
        }
    }

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
}