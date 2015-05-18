/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author vir
 */
public class TreeBuilder<T> {
    public static <T> TreeBuilder<T> createInstance(T value) {
        Node<T> root = new Node<T>(value, null);
        return new TreeBuilder<T>(root, root);
    }
    private final Node<T> root;
    private Node<T> current;
    
    private TreeBuilder(Node<T> root, Node<T> current) {
        this.root = root;
        this.current = current;
    }

    public void node(T value) {
        current.nodes.add(new Tree<T>(value, Collections.<Tree<T>>emptyList()));
    }

    public void subtree(Tree<T> tree) {
        current.nodes.add(tree);
    }

    public void subtree(T value, List<Tree<T>> children) {
        List<Tree<T>> unmodifiable = new ArrayList<Tree<T>>();
        unmodifiable.addAll(children);
        current.nodes.add(new Tree<T>(value, Collections.unmodifiableList(unmodifiable)));
    }

    public void beginSubtree(T value) {
        current = new Node<T>(value, current);
    }

    public void endSubtree() {
        Node<T> node = current;
        current = node.parent;
        current.nodes.add(node.build());
    }
    
    public Tree<T> build() {
        while (current != root)
            endSubtree();
        return root.build();
    }

    private static class Node<T> {
        private final T value;
        private final List<Tree<T>> nodes = new ArrayList<Tree<T>>();
        private final Node<T> parent;

        private Node(T value, Node<T> parent) {
            this.value = value;
            this.parent = parent;
        }

        private Tree<T> build() {
            List<Tree<T>> unmodifiable = new ArrayList<Tree<T>>();
            unmodifiable.addAll(nodes);
            return new Tree<T>(value, Collections.unmodifiableList(unmodifiable));
        }
    }
}
