package org.levelci.selenium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Engine analysis metadata ({@code AnalysisMeta} / {@code AnalysisReportMeta} in Level CI typings).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportMeta {

    @JsonProperty
    private String accessEngineVersion;

    @Builder.Default
    @JsonProperty
    private List<String> applicableRuleIds = new ArrayList<>();
}
