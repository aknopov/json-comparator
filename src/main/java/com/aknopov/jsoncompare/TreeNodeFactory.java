package com.aknopov.jsoncompare;

import javax.annotation.Nullable;

import com.aknopov.jsoncompare.TreeNode.NodeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Utility class for converting JsonNode's to TreeNode's.
 */
final class TreeNodeFactory
{
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
        return fromJackson("", jsonNode, null, 0);
    }

    /**
     * Converts {@code JsonNode} to {@code TreeNode} tree node using depth-first traversing
     *
     * @param name tree node name
     * @param jsonNode Jackson object
     * @param parent tree node parent
     * @param index index in the parent child list
     */
    static TreeNode<?> fromJackson(String name, JsonNode jsonNode, @Nullable TreeNode<?> parent, int index)
    {
        JsonNodeType jsonType = jsonNode.getNodeType();
        TreeNode<?> treeNode = switch (jsonType)
        {
            case OBJECT -> new TreeNode<>(name, NodeType.OBJECT, parent, null, index);
            case ARRAY -> new TreeNode<>(name, NodeType.ARRAY, parent, null, index);
            default -> valueToTreeNode(name, jsonNode, parent, index);
        };

        if (jsonType == JsonNodeType.OBJECT)
        {
            int idx = 0;
            for (String childName : fieldNames(jsonNode))
            {
                JsonNode jsonChild = jsonNode.get(childName);
                treeNode.addChild(fromJackson(childName, jsonChild, treeNode, idx++));
            }
        }
        else if (jsonType == JsonNodeType.ARRAY)
        {
            int idx = 0;
            for (JsonNode jsonChild : jsonNode)
            {
                treeNode.addChild(fromJackson("", jsonChild, treeNode, idx++));
            }
        }

        return treeNode;
    }

    private static TreeNode<?> valueToTreeNode(String fieldName, JsonNode jsonNode, @Nullable TreeNode<?> parent, int index)
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

    private static Iterable<String> fieldNames(JsonNode jsonNode)
    {
        return jsonNode::fieldNames;
    }
}
