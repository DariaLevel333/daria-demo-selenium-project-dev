package org.levelci.selenium.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.levelci.selenium.model.config.AnalysisConfig;

/**
 * Result of {@link org.levelci.selenium.AccessibilityAuditor#levelAnalyze}, mirroring the Level CI
 * {@code AnalysisResultData} interface: optional {@link #report}, optional {@link #error}, and
 * {@link #config} (launch options).
 * <p>
 * Outcome is inferred from presence/absence of {@code report} / {@code error}:
 * <ul>
 *   <li>{@code report} present, {@code error} null — analysis succeeded.</li>
 *   <li>{@code report} null, {@code error} present — analysis failed before producing findings.</li>
 *   <li>{@code report} present, {@code error} present — strict mode tripped on findings.</li>
 *   <li>both null — engine did not run (switched off or background-runner stub).</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private AnalysisReport report;

    private AnalysisError error;

    /** Effective launch/analysis config (after defaults and env overrides where applicable). */
    private AnalysisConfig config;

    public static AnalysisResult failed() {
        return AnalysisResult.builder()
                .error(AnalysisError.of("Analysis failed"))
                .build();
    }

    public static AnalysisResult failed(AnalysisConfig config) {
        return AnalysisResult.builder()
                .config(config)
                .error(AnalysisError.of("Analysis failed"))
                .build();
    }

    public static AnalysisResult failedOnStrict(AnalysisReport report, AnalysisConfig config) {
        return AnalysisResult.builder()
                .report(report)
                .config(config)
                .error(AnalysisError.of("Analysis failed on strict (rules with findings)"))
                .build();
    }

    /** Engine did not run (switched off or running under the background runner). */
    public static AnalysisResult didNotRun(AnalysisConfig config) {
        return AnalysisResult.builder()
                .config(config)
                .build();
    }

    public static AnalysisResult succeeded(AnalysisReport report, AnalysisConfig config) {
        return AnalysisResult.builder()
                .report(report)
                .config(config)
                .build();
    }

    /**
     * Total number of issues across all rules in the report.
     * Returns 0 if the report or its rules list is null.
     *
     * @return total issue count
     */
    public int getIssuesFound() {
        if (report == null || report.getRules() == null) {
            return 0;
        }
        return report.getRules().stream()
                .mapToInt(rule -> rule.getIssues() == null ? 0 : rule.getIssues().size())
                .sum();
    }
}
