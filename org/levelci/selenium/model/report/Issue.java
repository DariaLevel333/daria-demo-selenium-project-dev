package org.levelci.selenium.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors {@code AnalysisRuleIssue} from the access-engine analysis result ({@code rules[].issues[]}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Issue {
    @JsonProperty
    private String ruleId;
    @JsonProperty
    private String cssSelector;
    @JsonProperty
    private String pathSelector;
    @JsonProperty
    private String tag;
    /** Present only when the engine supplies it. */
    @JsonProperty
    private String snippet;
    @JsonProperty
    private String uel;
}
