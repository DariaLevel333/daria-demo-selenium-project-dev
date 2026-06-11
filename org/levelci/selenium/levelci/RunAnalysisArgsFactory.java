package org.levelci.selenium.levelci;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.levelci.selenium.model.config.AnalysisConfig;

import java.util.List;
import java.util.Set;

import static org.levelci.selenium.utils.Constants.MAPPER;

/**
 * Builds the config object passed to {@code LevelCiAccessEngine.runAnalysis(config)} from {@link AnalysisConfig}.
 * <p>
 * Mirrors the Level CI {@code LaunchConfig} surface: {@code ignoreUrls}, {@code customTags} and
 * {@code experimental.cssSelector} (the {@code switchOff} / {@code reportPath} flags are handled on
 * the Java side and not forwarded).
 */
public final class RunAnalysisArgsFactory {

    private RunAnalysisArgsFactory() {}

    public static ObjectNode build(AnalysisConfig c) {
        ObjectNode n = MAPPER.createObjectNode();
        List<String> ignoreUrls = c.getIgnoreUrls();
        if (ignoreUrls != null && !ignoreUrls.isEmpty()) {
            ArrayNode arr = n.putArray("ignoreUrls");
            for (String p : ignoreUrls) {
                arr.add(p);
            }
        }
        Set<String> tags = c.getCustomTags();
        if (tags != null && !tags.isEmpty()) {
            ArrayNode arr = n.putArray("customTags");
            for (String t : tags) {
                arr.add(t);
            }
        }
        ObjectNode experimental = buildExperimental(c.getExperimental());
        if (experimental != null) {
            n.set("experimental", experimental);
        }
        return n;
    }

    private static ObjectNode buildExperimental(AnalysisConfig.Experimental experimental) {
        if (experimental == null) {
            return null;
        }
        ObjectNode cssSelector = buildCssSelector(experimental.getCssSelector());
        if (cssSelector == null) {
            return null;
        }
        ObjectNode n = MAPPER.createObjectNode();
        n.set("cssSelector", cssSelector);
        return n;
    }

    private static ObjectNode buildCssSelector(AnalysisConfig.CssSelector cssSelector) {
        if (cssSelector == null) {
            return null;
        }
        ObjectNode n = MAPPER.createObjectNode();
        List<String> stableAttributes = cssSelector.getStableAttributes();
        if (stableAttributes != null && !stableAttributes.isEmpty()) {
            ArrayNode arr = n.putArray("stableAttributes");
            for (String a : stableAttributes) {
                arr.add(a);
            }
        }
        Boolean includeClasses = cssSelector.getIncludeClasses();
        if (includeClasses != null) {
            n.put("includeClasses", includeClasses);
        }
        List<String> ignoredClassPatterns = cssSelector.getIgnoredClassPatterns();
        if (ignoredClassPatterns != null && !ignoredClassPatterns.isEmpty()) {
            ArrayNode arr = n.putArray("ignoredClassPatterns");
            for (String p : ignoredClassPatterns) {
                arr.add(p);
            }
        }
        return n.isEmpty() ? null : n;
    }
}
