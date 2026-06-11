package org.levelci.selenium;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.levelci.selenium.model.AnalysisReport;
import org.levelci.selenium.model.AnalysisReportMeta;
import org.levelci.selenium.model.AnalysisResult;
import org.levelci.selenium.model.AnalysisRuleData;
import org.levelci.selenium.model.config.AnalysisConfig;
import org.levelci.selenium.model.report.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses access-engine {@code runAnalysis} JSON ({@code meta} + {@code rules}) into {@link AnalysisReport}.
 */
class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(AccessibilityAuditor.class);

    static AnalysisResult succeededFromRunResult(JsonNode runAnalysisRoot, AnalysisConfig config) {
        log.debug("Parsing engine analysis result");
        return AnalysisResult.succeeded(parseEngineReport(runAnalysisRoot), config);
    }

    static AnalysisReport parseEngineReport(JsonNode runAnalysisRoot) {
        if (runAnalysisRoot == null || runAnalysisRoot.isNull()) {
            return AnalysisReport.builder()
                    .meta(AnalysisReportMeta.builder().build())
                    .rules(List.of())
                    .build();
        }
        return AnalysisReport.builder()
                .meta(parseMeta(runAnalysisRoot.get("meta")))
                .rules(parseRules(runAnalysisRoot.get("rules")))
                .build();
    }

    private static AnalysisReportMeta parseMeta(JsonNode metaNode) {
        if (metaNode == null || !metaNode.isObject()) {
            return AnalysisReportMeta.builder().build();
        }
        List<String> applicable = new ArrayList<>();
        JsonNode ids = metaNode.get("applicableRuleIds");
        if (ids != null && ids.isArray()) {
            for (JsonNode id : ids) {
                if (!id.isNull()) {
                    applicable.add(id.asText(""));
                }
            }
        }
        return AnalysisReportMeta.builder()
                .accessEngineVersion(
                        metaNode.has("accessEngineVersion")
                                ? metaNode.get("accessEngineVersion").asText("")
                                : "")
                .applicableRuleIds(applicable)
                .build();
    }

    private static List<AnalysisRuleData> parseRules(JsonNode rulesNode) {
        List<AnalysisRuleData> rules = new ArrayList<>();
        if (rulesNode == null || !rulesNode.isArray()) {
            return rules;
        }
        for (JsonNode rule : rulesNode) {
            if (!rule.has("ruleId") || !rule.has("issues")) {
                continue;
            }
            String ruleId = rule.get("ruleId").asText();
            List<Issue> issues = new ArrayList<>();
            JsonNode issuesNode = rule.get("issues");
            if (issuesNode.isArray()) {
                for (JsonNode in : issuesNode) {
                    issues.add(mapIssue(in, ruleId));
                }
            }
            rules.add(AnalysisRuleData.builder().ruleId(ruleId).issues(issues).build());
        }
        return rules;
    }

    private static Issue mapIssue(JsonNode in, String ruleIdFromRule) {
        var issue = new Issue();
        issue.setRuleId(
                in.has("ruleId") && !in.get("ruleId").isNull() && !in.get("ruleId").asText("").isEmpty()
                        ? in.get("ruleId").asText()
                        : ruleIdFromRule);
        issue.setCssSelector(text(in, "cssSelector"));
        issue.setPathSelector(text(in, "pathSelector"));
        issue.setTag(text(in, "tag"));
        issue.setSnippet(optionalText(in, "snippet"));
        issue.setUel(text(in, "uel"));
        return issue;
    }

    private static String text(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return "";
        }
        return n.get(field).asText("");
    }

    private static String optionalText(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) {
            return null;
        }
        String v = n.get(field).asText();
        return v.isEmpty() ? null : v;
    }
}
