package com.aknopov.jsoncompare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TreeNodeTest
{
    private final static String NAME = "nodeName";
    private final static String CHILD_NAME = "childName";
    private static final int INDEX = 123;

    @Test
    void testCreation()
    {
        TreeNode<Void> node = new TreeNode<>(NAME, NodeType.OBJECT);
        assertEquals(NAME, node.getName());
        assertEquals(NodeType.OBJECT, node.getNodeType());
        assertNull(node.getParent());
        assertEquals(0, node.getChildren().length);
        assertNotEquals(0, node.getCrc32());

        TreeNode<Void> arrayNode = new TreeNode<>(NAME, NodeType.ARRAY);
        assertEquals(NAME, arrayNode.getName());
        assertEquals(NodeType.ARRAY, arrayNode.getNodeType());
        assertNull(arrayNode.getParent());
        assertEquals(0, arrayNode.getChildren().length);
        assertNotEquals(0, arrayNode.getCrc32());

        TreeNode<Long> valueNode = new TreeNode<>(NAME, NodeType.NUMBER, node, 777L, INDEX);
        assertEquals(NAME, valueNode.getName());
        assertEquals(NodeType.NUMBER, valueNode.getNodeType());
        assertEquals(node, valueNode.getParent());
        assertEquals(0, valueNode.getChildren().length);
        assertNotEquals(0, valueNode.getCrc32());
        assertEquals(INDEX, valueNode.getIndex());
        assertEquals(777L, valueNode.getValue());
    }

    @Test
    void testAddingChildren()
    {
        TreeNode<Void> root = new TreeNode<>(NAME, NodeType.OBJECT);
        TreeNode<String> child1 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root, "Hello");
        TreeNode<Double> child2 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root, 1.23);

        assertEquals(0, root.getChildren().length);
        var orgCrc = root.getCrc32();

        root.addChild(child1);
        assertEquals(1, root.getChildren().length);
        assertSame(root, child1.getParent());
        var crc1 = root.getCrc32();
        assertNotEquals(orgCrc, crc1);

        root.addChild(child2);
        assertEquals(2, root.getChildren().length);
        assertSame(root, child2.getParent());
        var crc2 = root.getCrc32();
        assertNotEquals(orgCrc, crc2);
        assertNotEquals(crc1, crc2);

        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }
}