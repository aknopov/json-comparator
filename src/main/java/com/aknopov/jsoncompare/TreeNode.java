package com.aknopov.jsoncompare;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32C;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * TreeNode presentation
 */
@Getter(AccessLevel.PACKAGE)
 class TreeNode<T>
{
    private final String name;
    private final NodeType nodeType;
    @Nullable
    private final T value;
    @Nullable
    private final TreeNode<?> parent;
    private final int index;
    @Getter(AccessLevel.NONE)
    private final List<TreeNode<?>> children;
    @Getter(AccessLevel.NONE)
    private final CRC32C crc32 = new CRC32C();

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

    int numChildren()
    {
        return children.size();
    }

    TreeNode<?>[] getChildren()
    {
        return children.toArray(new TreeNode[0]);
    }

    TreeNode<?> getChild(int index)
    {
        return children.get(index);
    }

    @SuppressWarnings("EnumOrdinal")
    private void initCrc32()
    {
        crc32.update(ByteBuffer.wrap(name.getBytes(Charset.defaultCharset())));
        crc32.update(nodeType.ordinal());
        if (parent != null)
        {
            crc32.update(parent.hashCode());
        }
        if (value != null)
        {
            crc32.update(value.hashCode());
        }
        crc32.update(index);
    }

    private void updateCrc(TreeNode<?> child)
    {
        crc32.update(child.hashCode());
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
                .append(Integer.toHexString(hashCode()));
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
        return crc32.getValue() == otherNode.crc32.getValue();
    }

    @Override
    public int hashCode()
    {
        return (int)crc32.getValue();
    }
}
