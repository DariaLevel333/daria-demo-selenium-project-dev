package org.levelci.selenium.model.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mirror of Level CI {@code LaunchConfig} (which extends an empty {@code AnalysisConfig}).
 * <p>
 * Passed to {@code LevelCiAccessEngine.runAnalysis} and used when building persisted reports from Java.
 */
@Builder
@Getter
@Setter
public class AnalysisConfig {

    /**
     * When {@code true}, {@link org.levelci.selenium.AccessibilityAuditor#levelAnalyze} skips running the engine.
     * Can be overridden via {@code LEVEL_CI_SWITCH_OFF}.
     */
    @Builder.Default
    @JsonProperty
    private Boolean switchOff = false;

    /**
     * Output directory for Level CI scope reports when persistence is enabled ({@code saveReport} on audit config).
     * Default on the Java side is {@link org.levelci.selenium.utils.Constants#A11Y_RESULTS_DEFAULT_FOLDER_PATH} when unset.
     */
    @JsonProperty
    private String reportPath;

    /**
     * URL patterns to ignore (regex source strings, same idea as {@code ignoreUrls?: RegExp[]} in Level CI).
     */
    @Builder.Default
    @JsonProperty
    private List<String> ignoreUrls = new ArrayList<>();

    /**
     * Tags attached to report metadata for dashboard filtering.
     */
    @Builder.Default
    @JsonProperty
    private Set<String> customTags = new HashSet<>();

    /**
     * Experimental options that may change without notice.
     */
    @JsonProperty
    private Experimental experimental;

    /**
     * Experimental options that may change without notice.
     */
    @Builder
    @Getter
    @Setter
    public static class Experimental {

        /**
         * CSS selector generation tuning.
         */
        @JsonProperty
        private CssSelector cssSelector;
    }

    /**
     * CSS selector generation tuning.
     */
    @Builder
    @Getter
    @Setter
    public static class CssSelector {

        /**
         * Attribute names treated as stable when building CSS selectors.
         * When unset, the access engine defaults to {@code ['id', 'data-testid']}.
         */
        @JsonProperty
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> stableAttributes;

        /**
         * When {@code true}, class names are included in generated CSS selectors.
         * When unset, the access engine defaults to {@code true}.
         */
        @JsonProperty
        private Boolean includeClasses;

        /**
         * Class name patterns (regex source strings) to ignore when building CSS selectors.
         */
        @Builder.Default
        @JsonProperty
        private List<String> ignoredClassPatterns = new ArrayList<>();
    }
}
