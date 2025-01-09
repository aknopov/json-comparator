package com.aknopov.jsoncompare;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TreeNodeFactoryTest
{
    private final static String EMPTY_SAMPLE = "{}";
    private final static String JSON_SAMPLE =
"""
{"a": {"b": "foo", "c": 5}, "d": {"e": "bar"}, "f": [13, 17, 31]}
""";
    private final static String ARRAY_SAMPLE = """
[
  {
    "id": 1,
    "name": {
      "first": "Nick",
      "last": "Cowtowncoder"
    },
    "contact": [
      {
        "type": "phone/home",
        "ref": "111-111-1234"
      },
      {
        "type": "phone/work",
        "ref": "222-222-2222"
      }
    ]
  },
  {
    "id": 2,
    "name": {
      "first": "Tatu",
      "last": "Saloranta"
    },
    "contact": [
      {
        "type": "phone/home",
        "ref": "333-333-1234"
      },
      {
        "type": "phone/work",
        "ref": "444-444-4444"
      }
    ]
  }
]
""";
    String SIMPLE_ARRAY = """
[{"id":1, "first": "alex"}, {"id":2, "first": "joe"}]""";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testEmptySample() throws Exception
    {
        JsonNode root = MAPPER.readTree(EMPTY_SAMPLE);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        assertNotNull(treeRoot);
        assertEquals(NodeType.OBJECT, treeRoot.getNodeType());
        assertEquals("", treeRoot.getName());
        assertNull(treeRoot.getValue());
        assertEquals(0, treeRoot.getChildren().length);

    }

    @Test
    void testSample() throws Exception
    {
        JsonNode root = MAPPER.readTree(JSON_SAMPLE);

        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);
        printTree(treeRoot, 0); //UC

        assertEquals(3, treeRoot.getChildren().length);

        TreeNode<?> node0 = treeRoot.getChild(0);
        assertEquals("a", node0.getName());
        assertEquals(NodeType.OBJECT, node0.getNodeType());
        assertEquals(-1, node0.getIndex());
        assertEquals(treeRoot, node0.getParent());
        assertEquals(2, node0.getChildren().length);
        TreeNode<?> node0_0 = node0.getChild(0);
        assertEquals(NodeType.TEXT, node0_0.getNodeType());
        assertEquals("foo", node0_0.getValue());
        TreeNode<?> node0_1 = node0.getChild(1);
        assertEquals(NodeType.NUMBER, node0_1.getNodeType());
        assertEquals(5.0, node0_1.getValue());

        TreeNode<?> node1 = treeRoot.getChild(1);
        assertEquals("d", node1.getName());
        assertEquals(NodeType.OBJECT, node1.getNodeType());
        assertEquals(1, node1.getChildren().length);

        TreeNode<?> node2 = treeRoot.getChild(2);
        assertEquals("f", node2.getName());
        assertEquals(NodeType.ARRAY, node2.getNodeType());
        assertEquals(3, node2.getChildren().length);
        assertEquals(NodeType.NUMBER, node2.getChild(0).getNodeType());
        assertEquals(13.0, node2.getChild(0).getValue());
        assertEquals(NodeType.NUMBER, node2.getChild(1).getNodeType());
        assertEquals(17.0, node2.getChild(1).getValue());
        assertEquals(NodeType.NUMBER, node2.getChild(2).getNodeType());
        assertEquals(31.0, node2.getChild(2).getValue());
    }

    @Test
    void testArray() throws Exception
    {
        JsonNode root = MAPPER.readTree(SIMPLE_ARRAY);
        TreeNode<?> treeRoot = TreeNodeFactory.fromJacksonRoot(root);

        printTree(treeRoot, 0); //UC

        assertEquals(NodeType.OBJECT, treeRoot.getNodeType());
        assertEquals("", treeRoot.getName());
        assertEquals(1, treeRoot.getChildren().length);

        TreeNode<?> arrayNode = treeRoot.getChild(0);
        assertEquals(NodeType.ARRAY, arrayNode.getNodeType());
        assertEquals(2, arrayNode.getChildren().length);
    }

    private void printTree(TreeNode<?> node, int level)
    {
        String margin = "  ".repeat(level);

        System.err.printf("%s%s=%s\n", margin, node.getName(), node);
        if (node.getNodeType() == NodeType.ARRAY)
        {
            System.err.printf("%s[\n", margin);
        }

        for (TreeNode<?> child: node.getChildren()) {
            printTree(child, level+1);
        }

        if (node.getNodeType() == NodeType.ARRAY)
        {
            System.err.printf("%s]\n", margin);
        }
    }
}