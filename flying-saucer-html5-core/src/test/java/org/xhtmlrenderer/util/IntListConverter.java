package org.xhtmlrenderer.util;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Arrays;
import java.util.regex.Pattern;

public class IntListConverter implements ArgumentConverter {

    private static final Pattern DELIMITERS = Pattern.compile(",\\s*");

    @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            if (!(source instanceof String input)) {
                throw new IllegalArgumentException("The argument should be a string: '%s'".formatted(source));
            }
            return Arrays.stream(DELIMITERS.split(input)).map(Integer::parseInt).toList();
        }
    }