package com.aknopov.jsoncompare;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Utility class for converting JsonNode's to TreeNode's.
 */
final class TreeNodeFactory
{
    private static final Set<JsonNodeType> VALUE_TYPES =
            Set.of(JsonNodeType.BINARY, JsonNodeType.BOOLEAN, JsonNodeType.NUMBER, JsonNodeType.STRING);

    private TreeNodeFactory()
    {
    }

    /**
     * Converts {@code JsonNode} to {@code TreeNode} tree root
     *
     * @param jsonNode Jackson object
     * @return converted tree
     */
    static TreeNode<?> fromJacksonRoot(JsonNode jsonNode)
    {
        TreeNode<?> treeRoot = new TreeNode<>("", NodeType.OBJECT, null, null);
        TreeNodeFactory.fromJackson(jsonNode, treeRoot, -1);
        return treeRoot;
    }

    /**
     * Converts {@code JsonNode} to {@code TreeNode} tree node using depth-first traversing
     *
     * @param jsonNode Jackson object
     * @param parent node parent
     * @param index index in the parent child list
     */
    static void fromJackson(JsonNode jsonNode, TreeNode<?> parent, int index)
    {
        if (VALUE_TYPES.contains(jsonNode.getNodeType()))
        {
            parent.addChild(valueToTreeNode("", jsonNode, parent, index));
            return;
        }
        else if (jsonNode.getNodeType() == JsonNodeType.ARRAY)
        {
            parent.addChild(arrayToTreeNode("", jsonNode, parent));
            return;
        }

        for (String fieldName : fieldNames(jsonNode))
        {
            JsonNode jsonChild = jsonNode.get(fieldName);
            switch (jsonChild.getNodeType())
            {
                case OBJECT ->
                {
                    TreeNode<Void> treeChild = new TreeNode<>(fieldName, NodeType.OBJECT, parent, null, -1);
                    parent.addChild(treeChild);
                    fromJackson(jsonChild, treeChild, -1);
                }
                case ARRAY ->
                {
                    parent.addChild(arrayToTreeNode(fieldName, jsonChild, parent));
                }
                case BINARY, BOOLEAN, NUMBER, STRING ->
                {
                    parent.addChild(valueToTreeNode(fieldName, jsonChild, parent, -1));
                }
            }
        }
    }

    private static TreeNode<?> valueToTreeNode(String fieldName, JsonNode jsonNode, TreeNode<?> parent, int index)
    {
        return switch (jsonNode.getNodeType())
        {
            case BINARY, STRING -> new TreeNode<>(fieldName, NodeType.TEXT, parent, jsonNode.asText(), index);
            case NUMBER -> new TreeNode<>(fieldName, NodeType.NUMBER, parent, jsonNode.asDouble(), index);
            case BOOLEAN -> new TreeNode<>(fieldName, NodeType.BOOLEAN, parent, jsonNode.asDouble(), index);
            default -> throw new IllegalArgumentException(
                    "Can't convert node of type '" + jsonNode.getNodeType() + "' to value");
        };
    }

    private static TreeNode<?> arrayToTreeNode(String fieldName, JsonNode jsonNode, TreeNode<?> parent)
    {
        TreeNode<Void> treeNode = new TreeNode<>(fieldName, NodeType.ARRAY, parent, null, -1);
        int i = 0;
        for (JsonNode arrayItem : jsonNode)
        {
            fromJackson(arrayItem, treeNode, i);
            i++;
        }

        return treeNode;
    }

    private static Iterable<String> fieldNames(JsonNode jsonNode)
    {
        return jsonNode::fieldNames;
    }
}
