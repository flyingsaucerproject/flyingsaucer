package org.xhtmlrenderer.util;

public class Reified {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> classOf(T... reified) {
        if (reified.length > 0) {
            throw new IllegalArgumentException("Please don't pass any values here. Java will detect the class automagically.");
        }
        return (Class<T>) reified.getClass().componentType();
    }
}
