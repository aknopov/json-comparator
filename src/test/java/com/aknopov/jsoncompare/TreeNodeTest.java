package com.aknopov.jsoncompare;

import org.junit.jupiter.api.Test;

import com.aknopov.jsoncompare.TreeNode.NodeType;

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
        assertEquals(0, node.getChildren().size());
        assertNotEquals(0, node.hashCode());

        TreeNode<Void> arrayNode = new TreeNode<>(NAME, NodeType.ARRAY);
        assertEquals(NAME, arrayNode.getName());
        assertEquals(NodeType.ARRAY, arrayNode.getNodeType());
        assertNull(arrayNode.getParent());
        assertEquals(0, arrayNode.getChildren().size());
        assertNotEquals(0, arrayNode.hashCode());

        TreeNode<Long> valueNode = new TreeNode<>(NAME, NodeType.NUMBER, node, 777L, INDEX);
        assertEquals(NAME, valueNode.getName());
        assertEquals(NodeType.NUMBER, valueNode.getNodeType());
        assertEquals(node, valueNode.getParent());
        assertEquals(0, valueNode.getChildren().size());
        assertNotEquals(0, valueNode.hashCode());
        assertEquals(INDEX, valueNode.getIndex());
        assertEquals(777L, valueNode.getValue());
    }

    @Test
    void testAddingChildren()
    {
        TreeNode<Void> root = new TreeNode<>(NAME, NodeType.OBJECT);
        TreeNode<String> child1 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root, "Hello");
        TreeNode<Double> child2 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root, 1.23);

        assertEquals(0, root.getChildren().size());
        var orgCrc = root.hashCode();

        root.addChild(child1);
        assertEquals(1, root.getChildren().size());
        assertSame(root, child1.getParent());
        var crc1 = root.hashCode();
        assertNotEquals(orgCrc, crc1);

        root.addChild(child2);
        assertEquals(2, root.getChildren().size());
        assertSame(root, child2.getParent());
        var crc2 = root.hashCode();
        assertNotEquals(orgCrc, crc2);
        assertNotEquals(crc1, crc2);

        assertEquals(child1, root.getChild(0));
        assertEquals(child2, root.getChild(1));
    }

    @Test
    void testHashConsistency()
    {
        TreeNode<Void> root1 = new TreeNode<>(NAME, NodeType.OBJECT);
        TreeNode<String> child11 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root1, "Hello");
        TreeNode<Double> child12 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root1, 1.23);
        root1.addChild(child11).addChild(child12);
        TreeNode<Void> root2 = new TreeNode<>(NAME, NodeType.OBJECT);
        TreeNode<String> child21 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root2, "Hello");
        TreeNode<Double> child22 = new TreeNode<>(CHILD_NAME, NodeType.TEXT, root2, 1.23);
        root2.addChild(child21).addChild(child22);

        assertEquals(root1, root2);
        assertEquals(root1.hashCode(), root2.hashCode());
    }

    @Test
    void testPathSerialization()
    {
        TreeNode<Void> root = new TreeNode<>("", NodeType.OBJECT);
        TreeNode<String> child1 = new TreeNode<>("child1", NodeType.OBJECT, root, null, 0);
        TreeNode<String> child1_1 = new TreeNode<>("child1_1", NodeType.OBJECT, child1, null, 0);
        TreeNode<String> child1_1_1 = new TreeNode<>("child1_1_1", NodeType.TEXT, child1_1, "Hello", 0);
        TreeNode<Double> child2 = new TreeNode<>("child2", NodeType.NUMBER, root, 1.0, 1);
        child1_1.addChild(child1_1_1);
        child1.addChild(child1_1);
        root.addChild(child1);
        root.addChild(child2);

        assertEquals("/", root.path());
        assertEquals("/child1[0]", child1.path());
        assertEquals("/child1[0]/child1_1", child1_1.path());
        assertEquals("/child1[0]/child1_1/child1_1_1", child1_1_1.path());
        assertEquals("/child2[1]", child2.path());
    }
}