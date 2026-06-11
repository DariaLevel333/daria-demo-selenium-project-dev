package org.levelci.selenium.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.util.HashSet;

/**
 * Selenium-side audit workflow: driver, timeouts, strict mode, and whether to persist reports.
 * Launch options ({@code switchOff}, {@code reportPath}, {@code ignoreUrls}, {@code customTags}) live in
 * {@link #analysisConfiguration} — same surface as Level CI {@code LaunchConfig}.
 */
@Builder
@Getter
@Setter
public class AuditConfig {

    private WebDriver driver;

    @Builder.Default
    private Duration auditTimeout = Duration.ofMinutes(10);

    private AnalysisConfig analysisConfiguration;

    /**
     * When true, writes scope reports under {@link AnalysisConfig#getReportPath()} (or the default reports folder).
     */
    @Builder.Default
    private boolean saveReport = false;

    @Builder.Default
    private boolean strict = false;

    @Builder.Default
    private boolean isBackgroundRunnerMode = false;

    public static AuditConfig deepCopy(AuditConfig src) {
        if (src == null) {
            return AuditConfig.builder()
                    .analysisConfiguration(AnalysisConfig.builder().build())
                    .build();
        }
        var analysisCfg = src.analysisConfiguration == null
                ? AnalysisConfig.builder().build()
                : src.analysisConfiguration;
        var tags = analysisCfg.getCustomTags() == null
                ? new HashSet<String>()
                : new HashSet<>(analysisCfg.getCustomTags());
        var ignoreUrls = analysisCfg.getIgnoreUrls() == null
                ? new java.util.ArrayList<String>()
                : new java.util.ArrayList<>(analysisCfg.getIgnoreUrls());
        return AuditConfig.builder()
                .driver(src.driver)
                .auditTimeout(src.auditTimeout)
                .analysisConfiguration(
                        AnalysisConfig.builder()
                                .switchOff(analysisCfg.getSwitchOff())
                                .reportPath(analysisCfg.getReportPath())
                                .ignoreUrls(ignoreUrls)
                                .customTags(tags)
                                .experimental(deepCopyExperimental(analysisCfg.getExperimental()))
                                .build())
                .saveReport(src.saveReport)
                .strict(src.strict)
                .isBackgroundRunnerMode(src.isBackgroundRunnerMode)
                .build();
    }

    private static AnalysisConfig.Experimental deepCopyExperimental(AnalysisConfig.Experimental src) {
        if (src == null) {
            return null;
        }
        return AnalysisConfig.Experimental.builder()
                .cssSelector(deepCopyCssSelector(src.getCssSelector()))
                .build();
    }

    private static AnalysisConfig.CssSelector deepCopyCssSelector(AnalysisConfig.CssSelector src) {
        if (src == null) {
            return null;
        }
        var stableAttributes = src.getStableAttributes() == null
                ? null
                : new java.util.ArrayList<String>(src.getStableAttributes());
        var ignoredClassPatterns = src.getIgnoredClassPatterns() == null
                ? new java.util.ArrayList<String>()
                : new java.util.ArrayList<>(src.getIgnoredClassPatterns());
        return AnalysisConfig.CssSelector.builder()
                .stableAttributes(stableAttributes)
                .includeClasses(src.getIncludeClasses())
                .ignoredClassPatterns(ignoredClassPatterns)
                .build();
    }
}
