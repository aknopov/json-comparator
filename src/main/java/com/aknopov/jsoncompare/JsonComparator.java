package com.aknopov.jsoncompare;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * The class implements comparison of JSON strings
 */
@Slf4j
public final class JsonComparator
{
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final boolean stopOnFirst;
    private final DiffRecorder diffRecorder;

    private JsonComparator(boolean stopOnFirst, Collection<String> knownDiscrepancies)
    {
        this.stopOnFirst = stopOnFirst;
        this.diffRecorder = new DiffRecorder(knownDiscrepancies);
    }

    /**
     * Compares two JSON strings. Stops comparison on the first discrepancy.
     *
     * @param sample1 first string
     * @param sample2 second string
     * @param stopOnFirst if true, stops on the first discrepancy
     *
     * @return list of discrepancies
     */
    public static List<String> compareJsonStrings(String sample1, String sample2, boolean stopOnFirst)
    {
        return compareJsonStrings(sample1, sample2, stopOnFirst, List.of());
    }

    /**
     * Compares two JSON strings. Stops comparison on the first discrepancy.
     *
     * @param sample1 first string
     * @param sample2 second string
     * @param stopOnFirst if true, stops on the first discrepancy
     * @param knownDiscrepancies list of acceptable discrepancies in RegEx format
     *
     * @return list of discrepancies
     */
    public static List<String> compareJsonStrings(String sample1, String sample2, boolean stopOnFirst,
            Collection<String> knownDiscrepancies)
    {
        JsonComparator comparator = new JsonComparator(stopOnFirst, knownDiscrepancies);

        JsonNode root1 = comparator.parseSample(sample1, "first");
        if (root1 != null || !stopOnFirst)
        {
            JsonNode root2 = comparator.parseSample(sample2, "second");
            if (root2 != null)
            {
                comparator.nodesEqual(root1, root2);
            }
        }


        return comparator.diffRecorder.getMessages();
    }

    @Nullable
    private JsonNode parseSample(String sample, String qualifier)
    {
        try
        {
            JsonNode ret = OBJECT_MAPPER.readTree(sample);
            if (ret.isEmpty())
            {
                log.error("Empty input for the {} sample", qualifier);
                diffRecorder.addMessage("Empty input for the " + qualifier + " sample");
                return null;

            }
            return ret;
        }
        catch (JsonProcessingException e)
        {
            log.error("Failed to parse the {} sample: {}", qualifier, e.getMessage());
            diffRecorder.addMessage("Failed to parse the " + qualifier + " sample: " + e.getOriginalMessage());
            return null;
        }
    }

    private void nodesEqual(JsonNode root1, JsonNode root2)
    {
        // UC
    }
}
