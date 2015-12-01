/*
 * Copyright (c) 2015, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.maven.plugin.versioning;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public class VersionSchema {
    private final SuffixMap suffixMap;
    private VersionSchema(SuffixMap suffixMap) {
        this.suffixMap = suffixMap;
    }
    
    boolean isPredecessorSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.orderIndex() < 0;
    }

    boolean isFinalSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.isFinalVersion();
    }

    int compareSuffixes(String suffix1, String suffix2) {
        Suffix suffixObject1 = suffixMap.getSuffix(suffix1);
        Suffix suffixObject2 = suffixMap.getSuffix(suffix2);
        int index1 = suffixObject1.orderIndex();
        int index2 = suffixObject2.orderIndex();
        int result = index1 < index2 ? -1 : (index1 == index2 ? 0 : 1);
        if (result != 0)
            return result;
        else {
            String canonical1 = suffixObject1.canonicalString();
            String canonical2 = suffixObject2.canonicalString();
            return canonical1.compareTo(canonical2);
        }
    }

    String getCanonicalSuffix(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.canonicalString();
    }

    String getCanonicalFinalSuffix() {
        Suffix suffixObject = suffixMap.getFinalSuffix();
        return suffixObject.canonicalString();
    }

    VersionComponent finalVersionComponent() {
        return VersionComponent.finalVersionComponent(this);
    }

    VersionComponentScanner createScanner(String versionString) {
        return VersionComponentScanner.createInstance(this, versionString);
    }

    VersionComponentInstance finalVersionComponentInstance(String separator) {
        return new VersionComponentInstance(separator, finalVersionComponent());
    }

    VersionComponent numbersComponent(int[] numbers) {
        return VersionComponent.numbers(this, numbers);
    }

    VersionComponent suffixComponent(String suffix) {
        return VersionComponent.suffix(this, suffix);
    }

    VersionComponentInstance numbersComponentInstance(String separator, int[] numbers) {
        return new VersionComponentInstance(separator, numbersComponent(numbers));
    }

    VersionComponentInstance suffixComponentInstance(String separator, String suffix) {
        return new VersionComponentInstance(separator, suffixComponent(suffix));
    }

    Version version(String version) {
        return new Version(this, version);
    }

    Version versionOf(VersionComponentInstance... components) {
        return Version.of(this, components);
    }

    String[] getPredecessorSuffixes() {
        SortedMap<Integer, Suffix> map = new TreeMap<Integer, Suffix>();
        for (Suffix suffix: suffixMap.suffixes()) {
            if (suffix.orderIndex() < 0)
                map.put(suffix.orderIndex(), suffix);
        }
        String[] result = new String[map.size()];
        int i = 0;
        for (Map.Entry<Integer, Suffix> entry: map.entrySet()) {
            result[i] = entry.getValue().canonicalString();
            i++;
        }
        return result;
    }

    String getSuffixDescription(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        return suffixObject.description();
    }

    String getNonEmptyFinalSuffix() {
        return suffixMap.nonEmptyFinalSuffixString();
    }

    String[] getSuffixVariants(String suffix) {
        Suffix suffixObject = suffixMap.getSuffix(suffix);
        Set<String> variants = suffixObject.variants();
        return variants.toArray(new String[variants.size()]);
    }

    private static class SuffixMap {
        private final Map<String, Suffix> map;
        private final Set<Suffix> suffixes;
        private final FinalSuffix finalSuffix;

        private SuffixMap(Map<String, Suffix> map, Set<Suffix> suffixes, FinalSuffix finalSuffix) {
            this.map = map;
            this.suffixes = suffixes;
            this.finalSuffix = finalSuffix;
        }

        Suffix getSuffix(String suffixString) {
            if (suffixString.isEmpty())
                return finalSuffix.suffix();
            else {
                Suffix suffix = map.get(suffixString);
                if (suffix != null)
                    return suffix;
                else {
                    SuffixVariants variants = new SuffixVariants(Collections.singleton(suffixString), suffixString);
                    Suffix defaultSuffix = new Suffix(1, null, variants);
                    return defaultSuffix;
                }
            }
        }

        public Suffix getFinalSuffix() {
            return finalSuffix.suffix();
        }

        public Iterable<Suffix> suffixes() {
            return suffixes;
        }

        public String nonEmptyFinalSuffixString() {
            return finalSuffix.nonEmptyCanocicalString();
        }
    }

    private static class Suffix {
        private final int orderIndex;
        private final String description;
        private final SuffixVariants variants;
        private Suffix(int orderIndex, String description, SuffixVariants variants) {
            this.orderIndex = orderIndex;
            this.description = description;
            this.variants = variants;
        }

        private int orderIndex() {
            return orderIndex;
        }

        private boolean isFinalVersion() {
            return orderIndex == 0;
        }

        private String canonicalString() {
            return variants.canonical();
        }

        private String description() {
            return description;
        }

        private Set<String> variants() {
            return variants.set();
        }
    }

    private static class FinalSuffix {
        private final Suffix suffix;
        private final String nonEmptyCanonicalString;

        private FinalSuffix(Suffix suffix, String nonEmptyCanonicalString) {
            this.suffix = suffix;
            this.nonEmptyCanonicalString = nonEmptyCanonicalString;
        }

        Suffix suffix() {
            return suffix;
        }

        String nonEmptyCanocicalString() {
            return nonEmptyCanonicalString;
        }
    }

    private static class SuffixVariants {
        private final Set<String> set;
        private final String canonical;
        private SuffixVariants(Set<String> set, String canonical) {
            this.set = set;
            this.canonical = canonical;
        }

        private Set<String> set() {
            return set;
        }

        private String canonical() {
            return canonical;
        }
    }

    static class Builder {
        private final Map<String, VersionSchema.SuffixBuilder> suffixBuilderMap = new TreeMap<String, VersionSchema.SuffixBuilder>();
        private final VersionSchema.SuffixBuilder finalSuffixBuilder = new SuffixBuilder(this);
        private boolean useNonEmptyFinalSuffix = false;

        SuffixBuilder getSuffixBuilder(String suffixString) {
            SuffixBuilder builder = suffixBuilderMap.get(suffixString);
            if (builder == null) {
                builder = new VersionSchema.SuffixBuilder(this);
                builder.addVariant(suffixString);
            }
            return builder;
        }

        private void putSuffixBuilder(String variant, SuffixBuilder suffix) {
            suffixBuilderMap.put(variant, suffix);
        }

        SuffixBuilder getFinalSuffixBuilder() {
            return finalSuffixBuilder;
        }

        void setUseNonEmptyFinalSuffix(boolean useNonEmptyFinalSuffix) {
            this.useNonEmptyFinalSuffix = useNonEmptyFinalSuffix;
        }

        VersionSchema build() {
            Set<SuffixBuilder> builders = new HashSet<SuffixBuilder>();
            builders.addAll(suffixBuilderMap.values());

            SortedSet<Integer> predecessorIndexes = new TreeSet<Integer>();
            SortedSet<Integer> successorIndexes = new TreeSet<Integer>();
            for (SuffixBuilder builder: builders) {
                if (builder.isPredecessor) {
                    predecessorIndexes.add(builder.index);
                } else {
                    successorIndexes.add(builder.index);
                }
            }

            Set<Suffix> set = new HashSet<Suffix>();
            Map<String, Suffix> map = new TreeMap<String, Suffix>();

            for (SuffixBuilder builder: builders) {
                if (!builder.isFinalVersion()) {
                    int index;
                    if (builder.isPredecessor)
                        index = builder.index - predecessorIndexes.last() - 1;
                    else
                        index = builder.index - successorIndexes.first() + 1;
                    Set<String> variants = new TreeSet<String>();
                    variants.addAll(builder.variants);
                    SuffixVariants suffixVariants = new SuffixVariants(Collections.unmodifiableSet(variants), builder.canonicalString);
                    Suffix suffix = new Suffix(index, builder.description, suffixVariants);
                    set.add(suffix);
                    for (String suffixString: variants)
                        map.put(suffixString, suffix);
                }
            }

            Set<String> variants = new TreeSet<String>();
            variants.addAll(finalSuffixBuilder.variants);
            String canonicalString = useNonEmptyFinalSuffix ? finalSuffixBuilder.canonicalString : "";
            SuffixVariants suffixVariants = new SuffixVariants(Collections.unmodifiableSet(variants), canonicalString);
            Suffix suffix = new Suffix(0, finalSuffixBuilder.description, suffixVariants);
            FinalSuffix finalSuffix = new FinalSuffix(suffix, finalSuffixBuilder.canonicalString);
            set.add(suffix);
            for (String suffixString: variants)
                map.put(suffixString, suffix);

            SuffixMap suffixMap = new SuffixMap(Collections.unmodifiableMap(map), Collections.unmodifiableSet(set), finalSuffix);
            return new VersionSchema(suffixMap);
        }
    }

    static class SuffixBuilder {
        String canonicalString = null;
        String description = null;
        private int index = 0;
        private boolean isPredecessor = false;
        private final Builder builder;
        Set<String> variants = new TreeSet<String>();

        private SuffixBuilder(Builder builder) {
            this.builder = builder;
        }

        void addVariant(String variant) {
            builder.putSuffixBuilder(variant, this);
            variants.add(variant);
            if (canonicalString == null)
                canonicalString = variant;
        }

        void setDescription(String description) {
            this.description = description;
        }

        void setCanonicalString(String suffixString) {
            canonicalString = suffixString;
        }

        void setOrderingIndex(int index) {
            this.index = index;
        }

        void setPredecessor(boolean isPredecessor) {
            this.isPredecessor = isPredecessor;
        }

        boolean isFinalVersion() {
            return this == builder.getFinalSuffixBuilder();
        }
    }
}
