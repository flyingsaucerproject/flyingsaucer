package org.xhtmlrenderer.pdf;

import org.jspecify.annotations.Nullable;
import org.xhtmlrenderer.css.constants.IdentValue;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparingInt;

public class FontFamily {
    private final String _name;
    private final List<FontDescription> _fontDescriptions = new ArrayList<>();

    FontFamily(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public List<FontDescription> getFontDescriptions() {
        return _fontDescriptions;
    }

    public void addFontDescription(FontDescription description) {
        _fontDescriptions.add(description);
        _fontDescriptions.sort(comparingInt(FontDescription::getWeight));
    }

    @Nullable
    public FontDescription match(int desiredWeight, IdentValue style) {
        List<FontDescription> candidates = new ArrayList<>();

        for (FontDescription description : _fontDescriptions) {
            if (description.getStyle() == style) {
                candidates.add(description);
            }
        }

        if (candidates.isEmpty()) {
            if (style == IdentValue.ITALIC) {
                return match(desiredWeight, IdentValue.OBLIQUE);
            } else if (style == IdentValue.OBLIQUE) {
                return match(desiredWeight, IdentValue.NORMAL);
            } else {
                candidates.addAll(_fontDescriptions);
            }
        }

        FontDescription result = findByWeight(candidates, desiredWeight, SearchMode.EXACT);

        if (result != null) {
            return result;
        } else {
            if (desiredWeight <= 500) {
                return findByWeight(candidates, desiredWeight, SearchMode.LIGHTER_OR_DARKER);
            } else {
                return findByWeight(candidates, desiredWeight, SearchMode.DARKER_OR_LIGHTER);
            }
        }
    }

    private enum SearchMode {
        EXACT,
        LIGHTER_OR_DARKER,
        DARKER_OR_LIGHTER
    }

    @Nullable
    private FontDescription findByWeight(List<FontDescription> matches, int desiredWeight, SearchMode searchMode) {
        return switch (searchMode) {
            case EXACT -> {
                for (FontDescription description : matches) {
                    if (description.getWeight() == desiredWeight) {
                        yield description;
                    }
                }
                yield null;
            }
            case LIGHTER_OR_DARKER -> {
                int offset;
                FontDescription description = null;
                for (offset = 0; offset < matches.size(); offset++) {
                    description = matches.get(offset);
                    if (description.getWeight() > desiredWeight) {
                        break;
                    }
                }

                if (offset > 0 && description.getWeight() > desiredWeight) {
                    yield matches.get(offset - 1);
                } else {
                    yield description;
                }

            }
            case DARKER_OR_LIGHTER -> {
                int offset;
                FontDescription description = null;
                for (offset = matches.size() - 1; offset >= 0; offset--) {
                    description = matches.get(offset);
                    if (description.getWeight() < desiredWeight) {
                        break;
                    }
                }

                if (offset != matches.size() - 1 && description != null && description.getWeight() < desiredWeight) {
                    yield matches.get(offset + 1);
                } else {
                    yield description;
                }
            }
        };
    }
}
