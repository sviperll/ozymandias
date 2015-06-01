/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep.resolution;

import com.github.sviperll.maven.profiledep.util.Tree;

/**
 *
 * @author vir
 */
@SuppressWarnings("serial")
public class ResolutionValidationException extends Exception {

    private final Tree<String> tree;

    ResolutionValidationException(Tree<String> tree) {
        super("Resolution error");
        this.tree = tree;
    }

    Tree<String> tree() {
        return tree;
    }

    public String renderResolutionTree() {
        return new ResolutionTree(tree).toString();
    }
}
