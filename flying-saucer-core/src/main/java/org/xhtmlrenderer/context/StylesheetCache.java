package org.xhtmlrenderer.context;

import org.xhtmlrenderer.css.sheet.Stylesheet;

import java.util.LinkedHashMap;

class StylesheetCache extends LinkedHashMap<String, Stylesheet> {
    private static final int cacheCapacity = 16;

    StylesheetCache() {
        super(cacheCapacity,  0.75f, true);
    }

    protected boolean removeEldestEntry(java.util.Map.Entry<String, Stylesheet> eldest) {
        return size() > cacheCapacity;
    }
}
