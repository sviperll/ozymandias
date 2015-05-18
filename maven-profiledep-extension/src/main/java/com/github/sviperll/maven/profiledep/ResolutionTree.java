/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sviperll.maven.profiledep;

import com.github.sviperll.maven.profiledep.util.Tree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vir
 */
public class ResolutionTree {
    private final Tree<String> tree;
    public ResolutionTree(Tree<String> tree) {
        this.tree = tree;
    }

    @Override
    public String toString() {
        Renderer renderer = new Renderer(">");
        renderer.renderHead(tree);
        renderer.renderChildren(tree);
        return renderer.toString();
    }

    Tree<String> tree() {
        return tree;
    }

    private static class Renderer {
        final StringBuilder result = new StringBuilder();
        List<String> prefix = new ArrayList<String>();

        private Renderer(String string) {
            prefix.add("> ");
        }
        private void renderHead(Tree<String> tree) {
            for (String s: prefix) {
                result.append(s);
            }
            result.append("---");
            result.append(tree.value());
            result.append("\n");
        }
        private void renderChildren(Tree<String> tree) {
            List<Tree<String>> children = tree.children();

            for (int i = 0; i < children.size() - 1; i++) {
                prefix.add(" |-");
                renderHead(children.get(i));
                prefix.remove(prefix.size() - 1);
                prefix.add(" | ");
                renderChildren(children.get(i));
                prefix.remove(prefix.size() - 1);
            }
            if (!children.isEmpty()) {
                prefix.add(" `-");
                renderHead(children.get(children.size() - 1));
                prefix.remove(prefix.size() - 1);
                prefix.add("   ");
                renderChildren(children.get(children.size() - 1));
                prefix.remove(prefix.size() - 1);
            }
        }

        @Override
        public String toString() {
            return result.toString();
        }
    }
}
