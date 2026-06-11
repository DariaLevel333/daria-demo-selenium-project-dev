package org.levelci.selenium.levelci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.levelci.selenium.model.config.AuditConfig;

import java.time.Instant;
import java.util.ArrayList;

import static org.levelci.selenium.utils.Constants.FRAMEWORK_NAME;
import static org.levelci.selenium.utils.Constants.MAPPER;

public final class AnalysisReportFactory {

    private AnalysisReportFactory() {}

    public static ObjectNode buildReport(
            JsonNode runAnalysisResult,
            AuditConfig config,
            String userAgent,
            int viewportWidth,
            int viewportHeight) {
        JsonNode engineMeta = runAnalysisResult.get("meta");
        if (engineMeta == null || !engineMeta.isObject()) {
            throw new IllegalArgumentException("runAnalysis result missing meta");
        }

        ObjectNode meta = MAPPER.createObjectNode();
        meta.put("date", Instant.now().toString());
        meta.set("artifacts", MAPPER.createObjectNode());
        meta.put("type", "WEB");
        meta.put("version", LevelCiConstants.REPORT_VERSION);

        ObjectNode env = MAPPER.createObjectNode();
        env.put("url", config.getDriver().getCurrentUrl());
        env.put("framework", FRAMEWORK_NAME);
        env.put("frameworkVersion", seleniumVersion(config));
        env.put("userAgent", userAgent == null ? "" : userAgent);
        BrowserInfo bi = BrowserInfo.fromUserAgent(userAgent);
        env.put("browser", bi.browser());
        env.put("browserVersion", bi.browserVersion());
        env.put("integrationVersion", LibraryInfo.integrationVersion());
        env.put("accessEngineVersion", engineMeta.path("accessEngineVersion").asText(""));
        env.set("applicableRulesIds", engineMeta.get("applicableRuleIds"));
        env.put("mode", config.isBackgroundRunnerMode() ? "background" : "manual");

        ObjectNode viewport = MAPPER.createObjectNode();
        viewport.put("width", viewportWidth);
        viewport.put("height", viewportHeight);
        env.set("viewport", viewport);

        var ac = config.getAnalysisConfiguration();
        var tags =
                ac == null || ac.getCustomTags() == null
                        ? new ArrayList<String>()
                        : new ArrayList<>(ac.getCustomTags());
        env.set("customTags", MAPPER.valueToTree(tags));

        meta.set("env", env);

        ObjectNode report = MAPPER.createObjectNode();
        report.set("meta", meta);
        JsonNode rules = runAnalysisResult.get("rules");
        report.set("rules", rules != null ? rules : MAPPER.createArrayNode());
        return report;
    }

    private static String seleniumVersion(AuditConfig config) {
        try {
            var pkg = RemoteWebDriver.class.getPackage();
            if (pkg != null && pkg.getImplementationVersion() != null) {
                return pkg.getImplementationVersion();
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}
