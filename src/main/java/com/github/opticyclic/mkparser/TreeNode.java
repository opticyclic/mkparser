package com.github.opticyclic.mkparser;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple tree structure
 */
public class TreeNode<T> {

    T data;
    TreeNode<T> parent;
    List<TreeNode<T>> children;

    public TreeNode(T data) {
        this.data = data;
        children = new LinkedList<>();
    }

    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<>(child);
        childNode.parent = this;
        children.add(childNode);
        return childNode;
    }

    @Override
    public String toString() {
        return printTree(0);
    }

    private static final int indent = 4;

    private String printTree(int increment) {
        String whitespace = "";
        for (int i = 0; i < increment; ++i) {
            whitespace = whitespace + " ";
        }
        String output = whitespace + data;
        for (TreeNode<T> child : children) {
            output += "\n" + child.printTree(increment + indent);
        }
        return output;
    }
}