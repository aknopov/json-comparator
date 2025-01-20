package com.aknopov.jsoncompare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.aknopov.jsoncompare.TreeNode.NodeType;
import com.aknopov.jsoncompare.diff.Diff;
import com.aknopov.jsoncompare.diff.DiffType;
import com.aknopov.jsoncompare.diff.MeyerAlgorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.extern.slf4j.Slf4j;

/**
 * The class implements comparison of JSON strings
 */
@Slf4j
public final class JsonComparator
{
    private final static ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();

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
            if (root1 != null && root2 != null)
            {
                TreeNode<?> treeRoot1 = TreeNodeConverter.fromJacksonRoot(root1);
                TreeNode<?> treeRoot2 = TreeNodeConverter.fromJacksonRoot(root2);
                comparator.nodesEqual(treeRoot1, treeRoot2);
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
            if (ret.isMissingNode())
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

    private boolean nodesEqual(TreeNode<?> node1, TreeNode<?> node2)
    {
        if (Objects.equals(node1, node2))
        {
            return true;
        }

        if (typesAreDifferent(node1, node2) && stopOnFirst)
        {
            return false;
        }
        if (namesAreDifferent(node1, node2) && stopOnFirst)
        {
            return false;
        }
        if (valuesAreDifferent(node1, node2) && stopOnFirst)
        {
            return false;
        }
        checkChildrenDifferences(node1, node2);
        return false;
    }

    private boolean typesAreDifferent(TreeNode<?> node1, TreeNode<?> node2)
    {
        NodeType type1 = node1.getNodeType();
        NodeType type2 = node2.getNodeType();
        if (type1 != type2)
        {
            diffRecorder.addMessage("Node types are different: '%s' vs '%s', path='%s'", type1, type2, node1.path());
            return true;
        }
        return false;
    }

    private boolean namesAreDifferent(TreeNode<?> node1, TreeNode<?> node2)
    {
        String name1 = node1.getName();
        String name2 = node2.getName();
        if (!Objects.equals(name1, name2))
        {
            diffRecorder.addMessage("Node names are different: '%s' vs '%s', path='%s'", name1, name2, node1.path());
            return true;
        }
        return false;
    }


    private boolean valuesAreDifferent(TreeNode<?> node1, TreeNode<?> node2)
    {
        Object value1 = node1.getValue();
        Object value2 = node2.getValue();
        if (!Objects.equals(value1, value2))
        {
            diffRecorder.addMessage("Nodes values differ: '%s' vs '%s', path='%s'", value1, value2, node1.path());
            return true;
        }
        return false;
    }

    private void checkChildrenDifferences(TreeNode<?> node1, TreeNode<?> node2)
    {
        List<TreeNode<?>> children1 = node1.getChildren();
        List<TreeNode<?>> children2 = node2.getChildren();
        if (Objects.equals(children1, children2))
        {
            return;
        }
        if (Objects.equals(sortChildren(children1), sortChildren(children2)))
        {
            diffRecorder.addMessage("Children order differ for %d nodes, path='%s'", children1.size(), node1.path());
            // TODO Implement comparison and output of sorted children
            return;
        }

        List<Diff<TreeNode<?>>> diffs = MeyerAlgorithm.compareSequences(children1, children2);

        BiMap<Diff<TreeNode<?>>, Diff<TreeNode<?>>> matchingMap = createModifiedNodesMap2(diffs);
        List<Diff<TreeNode<?>>> unmatchedDiffs = diffs.stream().filter(d -> !matchingMap.containsKey(d) && !matchingMap.containsValue(d))
                .toList();
        if (!unmatchedDiffs.isEmpty())
        {
            diffRecorder.addMessage("Children differ: counts %d vs %d (diffs: %s), path='%s'",
                    node1.getChildren().size(), node2.getChildren().size(),
                    extractNamesOrIndices(unmatchedDiffs, node1.getNodeType()), node1.path());
        }

        // Recursion!
        iterateMatchingNodes(matchingMap);
    }

    private List<TreeNode<?>> sortChildren(List<TreeNode<?>> children)
    {
        return children.stream()
                .sorted(Comparator.comparing(TreeNode::getName))
                .toList();
    }

    private BiMap<Diff<TreeNode<?>>, Diff<TreeNode<?>>> createModifiedNodesMap2(List<Diff<TreeNode<?>>> diffs)
    {
        HashBiMap<Diff<TreeNode<?>>, Diff<TreeNode<?>>> modifiedMap = HashBiMap.create();
        for (int i = 0; i < diffs.size(); i++)
        {
            Diff<TreeNode<?>> diff1 = diffs.get(i);
            if (modifiedMap.containsValue(diff1))
            {
                continue;
            }

            DiffType requiredDiff = diff1.t() == DiffType.DELETE ? DiffType.ADD : DiffType.DELETE;
            for (int j = i + 1; j < diffs.size(); j++)
            {
                Diff<TreeNode<?>> diff2 = diffs.get(j);
                if (modifiedMap.containsValue(diff2))
                {
                    continue;
                }

                if (diff2.t() == requiredDiff
                && Objects.equals(diff1.e().getName(), diff2.e().getName()))
                {
                    modifiedMap.put(diff1, diff2);
                    break;
                }
            }
        }
        return modifiedMap;
    }

    private void iterateMatchingNodes(BiMap<Diff<TreeNode<?>>, Diff<TreeNode<?>>> matchingMap)
    {
        for (Map.Entry<Diff<TreeNode<?>>, Diff<TreeNode<?>>> entry: matchingMap.entrySet())
        {
            TreeNode<?> node1 = entry.getKey().e();
            TreeNode<?> node2 = entry.getValue().e();
            if (!nodesEqual(node1, node2) && stopOnFirst)
            {
                return;
            }
        }
    }

    private String extractNamesOrIndices(List<Diff<TreeNode<?>>> unmatchedDiffs, NodeType parentType)
    {
        // First names from the first sample (deleted ones), then from the second (added ones)
        List<String> names = new ArrayList<>(unmatchedDiffs.size());
        if (parentType == NodeType.OBJECT)
        {
            extractNamesByEditType(names, unmatchedDiffs, DiffType.DELETE, "+");
            extractNamesByEditType(names, unmatchedDiffs, DiffType.ADD, "-");
        }
        else if (parentType == NodeType.ARRAY)
        {
            extractIndicesByEditType(names, unmatchedDiffs, DiffType.DELETE, "+");
            extractIndicesByEditType(names, unmatchedDiffs, DiffType.ADD, "-");
        }
        return String.join(", ", names);
    }

    // Extracts child names with run-length "compression" (just counting consecutive mismatches)
    private void extractNamesByEditType(List<String> names, List<Diff<TreeNode<?>>> unmatchedDiffs, DiffType diffType,
            String sign)
    {
        int startIdx = -1;
        int endIdx = -1;
        String firstName = "";
        for (Diff<TreeNode<?>> diff: unmatchedDiffs)
        {
            if (diff.t() == diffType)
            {
                if (startIdx != -1 && diff.aIdx() - startIdx == 1)
                {
                    endIdx = diff.aIdx();
                }
                else
                {
                    if (startIdx != -1)
                    {
                        recordRunConditionally(names, firstName, startIdx, endIdx, sign);
                    }
                    endIdx = diff.aIdx();
                    startIdx = endIdx;
                    firstName = diff.e().getName();
                }
            }
            else
            {
                recordRunConditionally(names, firstName, startIdx, endIdx, sign);
                startIdx = -1;
            }
        }
        recordRunConditionally(names, firstName, startIdx, endIdx, sign);
    }

    // Extract child indices with run-length "compression" (just counting consecutive mismatches)
    private void extractIndicesByEditType(List<String> names, List<Diff<TreeNode<?>>> unmatchedDiffs, DiffType diffType,
            String sign)
    {
        int startIdx = -1;
        int endIdx = -1;
        for (Diff<TreeNode<?>> diff: unmatchedDiffs)
        {
            if (diff.t() == diffType)
            {
                if (diff.aIdx() - startIdx == 1)
                {
                    endIdx = diff.aIdx();
                }
                else
                {
                    recordRunConditionally(names, "", startIdx, endIdx, sign);
                    endIdx = diff.aIdx();
                    startIdx = endIdx;
                }
            }
            else
            {
                recordRunConditionally(names, "", startIdx, endIdx, sign);
                startIdx = -1;
            }
        }
        recordRunConditionally(names, "", startIdx, endIdx, sign);
    }

    private void recordRunConditionally(List<String> names, String name, int startIdx, int endIdx, String sign)
    {
        if (startIdx != -1)
        {
            names.add(String.format("%s[%d-%d]:%s%d", name, startIdx, endIdx, sign, endIdx - startIdx + 1));
        }
    }
}
