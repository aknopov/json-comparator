package com.aknopov.jsoncompare;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
            if (root1 != null && root2 != null)
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
        TreeNode<?> treeRoot1 = TreeNodeFactory.fromJacksonRoot(root1);
        TreeNode<?> treeRoot2 = TreeNodeFactory.fromJacksonRoot(root2);

        if (Objects.equals(treeRoot1, treeRoot2))
        {
            return;
        }

        if (typesAreDifferent(treeRoot1, treeRoot2) && stopOnFirst)
        {
            return;
        }
        if (namesAreDifferent(treeRoot1, treeRoot2) && stopOnFirst)
        {
            return;
        }
        checkChildrenAreDifferent(treeRoot1, treeRoot2);
    }

    private boolean typesAreDifferent(TreeNode<?> treeNode1, TreeNode<?> treeNode2)
    {
        NodeType type1 = treeNode1.getNodeType();
        NodeType type2 = treeNode2.getNodeType();
        if (type1 != type2)
        {
            diffRecorder.addMessage("Node types are different: '%s' vs '%s'", type1, type2);
            return true;
        }
        return false;
    }

    private boolean namesAreDifferent(TreeNode<?> treeNode1, TreeNode<?> treeNode2)
    {
        String name1 = treeNode1.getName();
        String name2 = treeNode2.getName();
        if (!Objects.equals(name1, name2))
        {
            diffRecorder.addMessage("Node names are different: '%s' vs '%s'", name1, name2);
            return true;
        }
        return false;
    }

    private void checkChildrenAreDifferent(TreeNode<?> treeNode1, TreeNode<?> treeNode2)
    {
        TreeNode<?>[] children1 = treeNode1.getChildren();
        TreeNode<?>[] children2 = treeNode2.getChildren();
        if (Arrays.equals(children1, children2))
        {
            return;
        }
        if (Arrays.equals(sortChildren(children1), sortChildren(children2)))
        {
            diffRecorder.addMessage("Children order differ for %d nodes, path='%s'", children1.length, path(treeNode1));
            // TODO Implement comparison and output of sorted children
            return;
        }

        //UC
        diffRecorder.addMessage("Something is different for %d nodes, path='%s'", children1.length, path(treeNode1)); //UC
    }

    private TreeNode<?>[] sortChildren(TreeNode<?>[] children)
    {
        return Arrays.stream(children)
                .sorted(Comparator.comparing(TreeNode::getName))
                .toArray(TreeNode<?>[]::new);
    }

    private String path(TreeNode<?> treeNode)
    {
        ArrayDeque<String> path = new ArrayDeque<>();
        while (treeNode != null)
        {
            TreeNode<?> parentNode = treeNode.getParent();
            if (treeNode.numChildren() < 2)
            {
                path.push("/" + treeNode.getName());
            }
            else
            {
                path.push("/" + treeNode.getName() + "[" + treeNode.getIndex() + "]");
            }
            treeNode = parentNode;
        }
        return String.join("", path);
    }
}
