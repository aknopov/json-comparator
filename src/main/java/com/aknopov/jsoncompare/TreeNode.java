package com.aknopov.jsoncompare;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import lombok.Getter;

/**
 * TreeNode presentation
 */
@Getter
 class TreeNode<T>
{
    private final String name;
    private final NodeType nodeType;
    @Nullable
    private final T value;
    @Nullable
    private final TreeNode<?> parent;
    private final int index;
    private final List<TreeNode<?>> children;
    private int crc32;

    TreeNode(String name, NodeType nodeType)
    {
        this(name, nodeType, null, null);
    }

    TreeNode(String name, NodeType nodeType, @Nullable TreeNode<?> parent, T value)
    {
        this(name, nodeType, parent, value, -1);
    }

    TreeNode(String name, NodeType nodeType, @Nullable TreeNode<?> parent, @Nullable T value, int index)
    {
        this.name = name;
        this.nodeType = nodeType;
        this.value = value;
        this.parent = parent;
        this.index = index;
        this.children = new ArrayList<>();
        initCrc32();
    }

    TreeNode<T> addChild(TreeNode<?> child)
    {
        children.add(child);
        updateCrc(child);
        return this;
    }

    TreeNode<?>[] getChildren()
    {
        return children.toArray(new TreeNode[0]);
    }

    TreeNode<?> getChild(int index)
    {
        return children.get(index);
    }

    private void initCrc32()
    {
        this.crc32 = Objects.hash(name, nodeType, parent, value, index, children); //UC
    }

    private void updateCrc(TreeNode<?> child)
    {
        this.crc32 += 31 * this.crc32 + child.getCrc32(); //UC
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='")
                .append(name)
                .append('\'');
        sb.append(", nodeType=")
                .append(nodeType);
        sb.append(", value=")
                .append(value);
        sb.append(", root=")
                .append(parent==null);
        sb.append(", index=")
                .append(index);
        sb.append(", children=")
                .append(children.size());
        sb.append(", crc32=0x")
                .append(Integer.toHexString(crc32));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof TreeNode<?> otherNode))
        {
            return false;
        }
        return crc32 == otherNode.crc32;
    }

    @Override
    public int hashCode()
    {
        return crc32;
    }
}
