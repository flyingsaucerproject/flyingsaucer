package org.xhtmlrenderer.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static java.util.stream.Collectors.joining;

public class Html5Support {
    private static final Logger log = LoggerFactory.getLogger(Html5Support.class);

    public static void logUnsupportedFeatures(Set<String> tags, Set<String> cssFeatures) {
        if (!tags.isEmpty()) {
            log.warn("Encountered HTML5 elements which are not supported by FlyingSaucer: {}. Rendering may be incorrect.",
                tags.stream().map(tag -> String.format("<%s>", tag)).collect(joining(", ")));
        }

        if (!cssFeatures.isEmpty()) {
            log.warn("Encountered CSS3 features not supported by FlyingSaucer: {}. Rendering may be incorrect.",
                cssFeatures.stream().map(feature -> '"' + feature + '"').collect(joining(", ")));
        }
    }
}
