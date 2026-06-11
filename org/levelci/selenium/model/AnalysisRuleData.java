package org.levelci.selenium.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.levelci.selenium.model.report.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * One engine rule bucket ({@code AnalysisRuleData}): {@code ruleId} and {@code issues[]}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRuleData {

    @JsonProperty
    private String ruleId;

    @Builder.Default
    @JsonProperty
    private List<Issue> issues = new ArrayList<>();
}
