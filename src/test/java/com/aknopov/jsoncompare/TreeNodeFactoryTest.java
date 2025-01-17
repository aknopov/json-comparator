package com.aknopov.jsoncompare;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TreeNodeFactoryTest
{
    private final static String EMPTY_SAMPLE = "{}";
    private final static String JSON_SAMPLE =
"""
{"a": {"b": "foo", "c": 5}, "d": {"e": "bar"}, "f": [13, 17, 31]}
""";
    private final static String ARRAY_SAMPLE = """
[{"id":1, "first": "alex"}, {"id":2, "first": "joe"}]""";
    private final static String ARRAY_2D = "[[1,2],[3,4]]";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testEmptySample() throws Exception
    {
        JsonNode root = MAPPER.readTree(EMPTY_SAMPLE);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        assertEquals(NodeType.OBJECT, treeRoot.getNodeType());
        assertEquals("", treeRoot.getName());
        assertNull(treeRoot.getValue());
        assertEquals(0, treeRoot.getIndex());
        assertEquals(0, treeRoot.numChildren());
    }

    @Test
    void testSample() throws Exception
    {
        JsonNode root = MAPPER.readTree(JSON_SAMPLE);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        assertEquals(3, treeRoot.numChildren());

        TreeNode<?> node0 = treeRoot.getChild(0);
        assertEquals("a", node0.getName());
        assertEquals(NodeType.OBJECT, node0.getNodeType());
        assertEquals(0, node0.getIndex());
        assertEquals(treeRoot, node0.getParent());
        assertEquals(2, node0.numChildren());
        {
            TreeNode<?> node0_0 = node0.getChild(0);
            assertEquals(NodeType.TEXT, node0_0.getNodeType());
            assertEquals("foo", node0_0.getValue());
            assertEquals(0, node0_0.getIndex());
            TreeNode<?> node0_1 = node0.getChild(1);
            assertEquals(NodeType.NUMBER, node0_1.getNodeType());
            assertEquals(5.0, node0_1.getValue());
            assertEquals(1, node0_1.getIndex());
        }

        TreeNode<?> node1 = treeRoot.getChild(1);
        assertEquals("d", node1.getName());
        assertEquals(NodeType.OBJECT, node1.getNodeType());
        assertEquals(1, node1.getIndex());
        assertEquals(1, node1.numChildren());
        {
            TreeNode<?> node2 = treeRoot.getChild(2);
            assertEquals("f", node2.getName());
            assertEquals(NodeType.ARRAY, node2.getNodeType());
            assertEquals(2, node2.getIndex());
            assertEquals(3, node2.numChildren());
            assertEquals(NodeType.NUMBER, node2.getChild(0).getNodeType());
            assertEquals(13.0, node2.getChild(0).getValue());
            assertEquals(NodeType.NUMBER, node2.getChild(1).getNodeType());
            assertEquals(17.0, node2.getChild(1).getValue());
            assertEquals(NodeType.NUMBER, node2.getChild(2).getNodeType());
            assertEquals(31.0, node2.getChild(2).getValue());
        }
    }

    @Test
    void testArray() throws Exception
    {
        JsonNode root = MAPPER.readTree(ARRAY_SAMPLE);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        assertEquals(NodeType.ARRAY, treeRoot.getNodeType());
        assertEquals("", treeRoot.getName());
        assertEquals(2, treeRoot.numChildren());

        TreeNode<?> node0 = treeRoot.getChild(0);
        assertEquals(NodeType.OBJECT, node0.getNodeType());
        assertEquals("", node0.getName());
        assertEquals(0, node0.getIndex());
        assertEquals(2, node0.numChildren());
        {
            TreeNode<?> node0_0 = node0.getChild(0);
            assertEquals("id", node0_0.getName());
            assertEquals(NodeType.NUMBER, node0_0.getNodeType());
            assertEquals(1.0, node0_0.getValue());
            assertEquals(0, node0_0.getIndex());
            TreeNode<?> node0_1 = node0.getChild(1);
            assertEquals("first", node0_1.getName());
            assertEquals(NodeType.TEXT, node0_1.getNodeType());
            assertEquals("alex", node0_1.getValue());
            assertEquals(1, node0_1.getIndex());
        }
        TreeNode<?> node1 = treeRoot.getChild(1);
        assertEquals(NodeType.OBJECT, node1.getNodeType());
        assertEquals("", node1.getName());
        assertEquals(1, node1.getIndex());
        assertEquals(2, node1.numChildren());
        {
            TreeNode<?> node1_0 = node1.getChild(0);
            assertEquals("id", node1_0.getName());
            assertEquals(NodeType.NUMBER, node1_0.getNodeType());
            assertEquals(2.0, node1_0.getValue());
            assertEquals(0, node1_0.getIndex());
            TreeNode<?> node1_1 = node1.getChild(1);
            assertEquals("first", node1_1.getName());
            assertEquals(NodeType.TEXT, node1_1.getNodeType());
            assertEquals("joe", node1_1.getValue());
            assertEquals(1, node1_1.getIndex());
        }
    }

    @Test
    void test2dArray() throws Exception
    {
        JsonNode root = MAPPER.readTree(ARRAY_2D);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        assertEquals(NodeType.ARRAY, treeRoot.getNodeType());
        assertEquals("", treeRoot.getName());
        assertEquals(2, treeRoot.numChildren());

        TreeNode<?> child0 = treeRoot.getChild(0);
        assertEquals(NodeType.ARRAY, child0.getNodeType());
        assertEquals("", child0.getName());
        assertEquals(2, child0.numChildren());
        {
            TreeNode<?> child0_0 = child0.getChild(0);
            assertEquals(NodeType.NUMBER, child0_0.getNodeType());
            assertEquals(1.0, child0_0.getValue());
            assertEquals("", child0_0.getName());
            TreeNode<?> child0_1 = child0.getChild(1);
            assertEquals(NodeType.NUMBER, child0_1.getNodeType());
            assertEquals(2.0, child0_1.getValue());
            assertEquals("", child0_1.getName());
        }
        TreeNode<?> child1 = treeRoot.getChild(1);
        assertEquals(NodeType.ARRAY, child1.getNodeType());
        assertEquals("", child1.getName());
        assertEquals(2, child1.numChildren());
        {
            TreeNode<?> child1_0 = child1.getChild(0);
            assertEquals(NodeType.NUMBER, child1_0.getNodeType());
            assertEquals(3.0, child1_0.getValue());
            assertEquals("", child1_0.getName());
            TreeNode<?> child1_1 = child1.getChild(1);
            assertEquals(NodeType.NUMBER, child1_1.getNodeType());
            assertEquals(4.0, child1_1.getValue());
            assertEquals("", child1_1.getName());
        }
    }
}