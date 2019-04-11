package cn.easysb.ip.redblacktree;

import lombok.Data;

// class RedBlackNode
@Data
public class RedBlackNode<T extends Comparable<T>> {

    /**
     * Possible color for this node
     */
    public static final int BLACK = 0;
    /**
     * Possible color for this node
     */
    public static final int RED = 1;
    // the key of each node
    public T key;

    /**
     * 新增字段，用于表示最小最大值
     */
    public T minValue;
    public T maxValue;

    /**
     * Parent of node
     */
    RedBlackNode<T> parent;
    /**
     * Left child
     */
    RedBlackNode<T> left;
    /**
     * Right child
     */
    RedBlackNode<T> right;
    // the number of elements to the left of each node
    public int numLeft = 0;
    // the number of elements to the right of each node
    public int numRight = 0;
    // the color of a node
    public int color;

    public RedBlackNode() {
        reset();
    }

    // Constructor which sets key to the argument.
    public RedBlackNode(T key) {
        reset();
        this.key = key;
    }

    public void reset() {
        color = BLACK;
        numLeft = 0;
        numRight = 0;
        parent = null;
        key = null;
        minValue = null;
        maxValue = null;
        left = null;
        right = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(color == BLACK ? "black," : "red,")
                .append(key == null ? "null" : key.toString())
                .append(", parent:")
                .append(parent != null && parent.key != null ? parent.key.toString() : "null")
                .append(", left:")
                .append(left != null && left.key != null ? left.key.toString() : "null")
                .append(", right:")
                .append(right != null && right.key != null ? right.key.toString() : "null");
        return sb.toString();
    }
}// end class RedBlackNode

