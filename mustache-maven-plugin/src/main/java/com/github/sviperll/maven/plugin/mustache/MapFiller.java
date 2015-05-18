/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class MapFiller {
    private final Map<String, Object> result;

    MapFiller(Map<String, Object> result) {
        this.result = result;
    }

    @SuppressWarnings("unchecked")
    void put(String key, String value) {
        int index = key.indexOf('.');
        if (index < 0)
            result.put(key, value);
        else {
            String prefix = key.substring(0, index);
            Object mapObject = result.get(prefix);
            Map<String, Object> map;
            if (mapObject != null && mapObject instanceof Map)
                map = (Map<String, Object>)mapObject;
            else {
                map = new TreeMap<String, Object>();
                result.put(prefix, map);
            }
            MapFiller filler = new MapFiller(map);
            filler.put(key.substring(index + 1), value);
        }
    }

}
