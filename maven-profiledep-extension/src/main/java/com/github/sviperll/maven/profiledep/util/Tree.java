/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.util;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author vir
 */
public class Tree<T> {
    private final T value;
    private final List<Tree<T>> children;

    public Tree(T value, List<Tree<T>> children) {
        this.value = value;
        this.children = children;
    }

    public T value() {
        return value;
    }

    public List<Tree<T>> children() {
        return children;
    }
}
