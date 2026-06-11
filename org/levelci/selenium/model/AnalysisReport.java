package org.levelci.selenium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Shape returned by the access engine and persisted as {@code rules} under scope reports
 * ({@code AnalysisReport} in Level CI: {@code meta} + {@code rules}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {

    @JsonProperty
    private AnalysisReportMeta meta;

    @Builder.Default
    @JsonProperty
    private List<AnalysisRuleData> rules = new ArrayList<>();
}
