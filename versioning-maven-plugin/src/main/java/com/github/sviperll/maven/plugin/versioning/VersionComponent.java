/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class VersionComponent implements Comparable<VersionComponent> {

    private static VersionComponent special(final SpecialKind kind) {
        return new VersionComponent() {
            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.special(kind);
            }
        };
    }

    static VersionComponent alpha() {
        return special(SpecialKind.ALPHA);
    }

    static VersionComponent beta() {
        return special(SpecialKind.BETA);
    }

    static VersionComponent rc() {
        return special(SpecialKind.RC);
    }

    static VersionComponent snapshot() {
        return special(SpecialKind.SNAPSHOT);
    }

    static VersionComponent finalVersion() {
        return special(SpecialKind.FINAL);
    }

    static VersionComponent numbers(final int[] v) {
        return new VersionComponent() {
            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.numbers(v);
            }
        };
    }

    static VersionComponent custom(final String s) {
        String lower = s.toLowerCase(Locale.US);
        if (lower.equals("a") || lower.equals("alpha"))
            return VersionComponent.special(SpecialKind.ALPHA);
        else if (lower.equals("b") || lower.equals("beta"))
            return VersionComponent.special(SpecialKind.BETA);
        else if (lower.equals("rc"))
            return VersionComponent.special(SpecialKind.RC);
        else if (lower.equals("snapshot"))
            return VersionComponent.special(SpecialKind.SNAPSHOT);
        else if (lower.equals("") || lower.equals("final"))
            return VersionComponent.special(SpecialKind.FINAL);
        else {
            for (SpecialKind kind: SpecialKind.values()) {
                if (lower.equals(kind.toString().toLowerCase(Locale.US)))
                    return VersionComponent.special(kind);
            }
            return new VersionComponent() {
                @Override
                <R> R accept(Visitor<R> visitor) {
                    return visitor.custom(s);
                }
            };
        }
    }

    private VersionComponent() {
    }

    abstract <R> R accept(Visitor<R> visitor);

    @Override
    public int compareTo(final VersionComponent that) {
        return accept(new CompareVisitor(that));
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        else if (!(thatObject instanceof VersionComponent))
            return false;
        else {
            VersionComponent that = (VersionComponent)thatObject;
            return this.compareTo(that) == 0;
        }
    }

    @Override
    public int hashCode() {
        return accept(new HashCodeVisitor());
    }

    @Override
    public String toString() {
        return accept(new ToStringVisitor());
    }

    VersionComponentInstance withTheSameSeparator(VersionComponentInstance component) {
        return component.withTheSameSeparator(this);
    }

    boolean isNumbers() {
        return accept(new IsNumbersVisitor());
    }

    boolean isExtensible() {
        return !isFinal();
    }

    boolean isSpecial() {
        return accept(new IsSpecialVisitor());
    }

    SpecialKind specialKind() {
        return accept(new GetSpecialKindVisitor());
    }

    boolean isFinal() {
        return isSpecial() && specialKind().equals(SpecialKind.FINAL);
    }

    List<VersionComponent> nextNumbersComponentVariants(int minDepth, int maxDepth) {
        return accept(new NextNumbersComponentVariants(minDepth, maxDepth, false));
    }

    List<VersionComponent> deepNextNumbersComponentVariants(int minDepth, int maxDepth) {
        return accept(new NextNumbersComponentVariants(minDepth, maxDepth, true));
    }

    String defaultSeparator() {
        return isFinal() ? "" : "-";
    }

    interface Visitor<R> {
        R special(SpecialKind kind);
        R numbers(int[] v);
        R custom(String s);
    }

    enum SpecialKind {
        ALPHA("alpha"), BETA("beta"), RC("rc"), SNAPSHOT("snapshot"), FINAL("");

        private final String representation;
        private SpecialKind(String representation) {
            this.representation = representation;
        }
        @Override
        public String toString() {
            return representation;
        }
    }

    private static class CompareVisitor implements Visitor<Integer> {

        private final VersionComponent that;

        public CompareVisitor(VersionComponent that) {
            this.that = that;
        }

        @Override
        public Integer special(final SpecialKind thisKind) {
            return that.accept(new SpecialCompareVisitor(thisKind));
        }

        @Override
        public Integer numbers(final int[] thisV) {
            return that.accept(new NumbersCompareVisitor(thisV));
        }

        @Override
            public Integer custom(final String thisS) {
                return that.accept(new CustomCompareVisitor(thisS));
            }

        private static class SpecialCompareVisitor implements Visitor<Integer> {

            private final SpecialKind thisKind;

            public SpecialCompareVisitor(SpecialKind thisKind) {
                this.thisKind = thisKind;
            }

            @Override
            public Integer special(SpecialKind thatKind) {
                return thisKind.compareTo(thatKind);
            }

            @Override
            public Integer numbers(int[] v) {
                return -1;
            }

            @Override
            public Integer custom(String s) {
                return -1;
            }
        }

        private static class NumbersCompareVisitor implements Visitor<Integer> {

            private final int[] thisV;

            public NumbersCompareVisitor(int[] thisV) {
                this.thisV = thisV;
            }

            @Override
            public Integer special(SpecialKind kind) {
                return 1;
            }

            @Override
            public Integer numbers(int[] thatV) {
                for (int i = 0; ; i++) {
                    if (i >= thisV.length && i >= thatV.length)
                        break;
                    else if (i >= thisV.length)
                        return -1;
                    else if (i >= thatV.length)
                        return 1;
                    else {
                        int thisE = thisV[i];
                        int thatE = thatV[i];
                        int result = thisE < thatE ? -1 : (thisE == thatE ? 0 : 1);
                        if (result != 0)
                            return result;
                    }
                }
                return 0;
            }

            @Override
                public Integer custom(String s) {
                    return -1;
                }
        }

        private static class CustomCompareVisitor implements Visitor<Integer> {

            private final String thisS;

            public CustomCompareVisitor(String thisS) {
                this.thisS = thisS;
            }

            @Override
            public Integer special(SpecialKind kind) {
                return 1;
            }

            @Override
            public Integer numbers(int[] v) {
                return 1;
            }

            @Override
            public Integer custom(String thatS) {
                return thisS.compareTo(thatS);
            }
        }
    }

    private static class HashCodeVisitor implements Visitor<Integer> {

        public HashCodeVisitor() {
        }

        @Override
        public Integer special(SpecialKind kind) {
            return 5 * 37 + kind.hashCode();
        }

        @Override
        public Integer numbers(int[] v) {
            int result = 6;
            for (int i = 0; i < v.length; i++) {
                result = result * 37 + v[i];
            }
            return result;
        }

        @Override
        public Integer custom(String s) {
            return 7 * 37 + s.hashCode();
        }
    }

    private static class ToStringVisitor implements Visitor<String> {

        public ToStringVisitor() {
        }

        @Override
        public String special(SpecialKind kind) {
            return kind.toString();
        }

        @Override
        public String numbers(int[] v) {
            StringBuilder builder = new StringBuilder();
            if (v.length > 0) {
                builder.append(v[0]);
                for (int i = 1; i < v.length; i++) {
                    builder.append('.');
                    builder.append(v[i]);
                }
            }
            return builder.toString();
        }

        @Override
        public String custom(String s) {
            return s;
        }
    }

    private static class IsNumbersVisitor implements Visitor<Boolean> {

        public IsNumbersVisitor() {
        }

        @Override
        public Boolean special(SpecialKind kind) {
            return false;
        }

        @Override
        public Boolean numbers(int[] v) {
            return true;
        }

        @Override
        public Boolean custom(String s) {
            return false;
        }
    }

    private static class IsSpecialVisitor implements Visitor<Boolean> {

        @Override
        public Boolean special(SpecialKind kind) {
            return true;
        }

        @Override
        public Boolean numbers(int[] v) {
            return false;
        }

        @Override
        public Boolean custom(String s) {
            return false;
        }
    }

    private static class NextNumbersComponentVariants implements Visitor<List<VersionComponent>> {

        private final int maxDepth;
        private final int minDepth;
        private final boolean goesDeeper;

        public NextNumbersComponentVariants(int minDepth, int maxDepth, boolean goesDeeper) {
            this.maxDepth = maxDepth;
            this.minDepth = minDepth;
            this.goesDeeper = goesDeeper;
        }

        @Override
        public List<VersionComponent> special(SpecialKind kind) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<VersionComponent> numbers(int[] v) {
            List<VersionComponent> result = new ArrayList<VersionComponent>();
            int reachableDepth = goesDeeper ? maxDepth : Math.min(v.length, maxDepth);
            for (int depth = minDepth; depth <= reachableDepth; depth++) {
                for (int i = 0; i < depth; i++) {
                    int[] variant = Arrays.copyOf(v, depth);
                    variant[i] = i < v.length ? v[i] + 1 : 1;
                    for (int j = i + 1; j < depth; j++)
                        variant[j] = 0;
                    result.add(VersionComponent.numbers(variant));
                }
            }
            return result;
        }

        @Override
        public List<VersionComponent> custom(String s) {
            throw new UnsupportedOperationException();
        }
    }

    private static class GetSpecialKindVisitor implements Visitor<SpecialKind> {

        @Override
        public SpecialKind special(SpecialKind kind) {
            return kind;
        }

        @Override
        public SpecialKind numbers(int[] v) {
            throw new UnsupportedOperationException("Not special.");
        }

        @Override
        public SpecialKind custom(String s) {
            throw new UnsupportedOperationException("Not special.");
        }
    }
}
