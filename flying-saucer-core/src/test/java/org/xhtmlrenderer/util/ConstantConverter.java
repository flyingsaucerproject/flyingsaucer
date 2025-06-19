package org.xhtmlrenderer.util;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.lang.reflect.Field;

import static java.util.Objects.requireNonNull;

public abstract class ConstantConverter<T> implements ArgumentConverter {
    private final Class<T> klass;

    @SuppressWarnings("unchecked")
    protected ConstantConverter(T... reified) {
        if (reified.length > 0) {
            throw new IllegalArgumentException("Please don't pass any values here. Java will detect page object class automagically.");
        }
        this.klass = (Class<T>) reified.getClass().componentType();
    }

    protected ConstantConverter(Class<T> klass) {
        this.klass = klass;
    }

    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (!(source instanceof String input)) {
            throw new IllegalArgumentException("The argument should be a string: '%s'".formatted(source));
        }
        try {
            Field field = klass.getDeclaredField(input);
            field.setAccessible(true);
            return requireNonNull(field.get(null), () -> "Unknown %s: '%s'".formatted(klass.getName(), source));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ArgumentConversionException("Cannot get constant '%s.%s'".formatted(klass.getName(), source), e);
        }
    }
}
