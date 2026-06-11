package org.levelci.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.levelci.selenium.model.config.AnalysisConfig;
import org.levelci.selenium.model.config.AuditConfig;

import java.util.Map;

import static org.levelci.selenium.EnvironmentVariable.*;

class EnvironmentService {

    private static final Logger log = LoggerFactory.getLogger(AccessibilityAuditor.class);

    private final Map<String, String> environment;

    EnvironmentService(Map<String, String> environment) {
        this.environment = environment;
    }

    void overrideWithEnvironmentValues(AuditConfig config) {
        if (config.getAnalysisConfiguration() == null) {
            config.setAnalysisConfiguration(AnalysisConfig.builder().build());
        }
        var saveReport = valBool(LEVEL_CI_SAVE_JSON_REPORT);
        var reportPath = val(LEVEL_CI_REPORT_PATH);
        var switchOff = valBool(LEVEL_CI_SWITCH_OFF);
        var strict = valBool(LEVEL_CI_STRICT);

        if (saveReport != null) {
            log.debug("Overriding with ENV value {}={}", LEVEL_CI_SAVE_JSON_REPORT, saveReport);
            config.setSaveReport(saveReport);
        }

        if (reportPath != null) {
            log.debug("Overriding with ENV value {}={}", LEVEL_CI_REPORT_PATH, reportPath);
            config.getAnalysisConfiguration().setReportPath(reportPath);
        }

        if (switchOff != null) {
            log.debug("Overriding with ENV value {}={}", LEVEL_CI_SWITCH_OFF, switchOff);
            config.getAnalysisConfiguration().setSwitchOff(switchOff);
        }

        if (strict != null) {
            log.debug("Overriding with ENV value {}={}", LEVEL_CI_STRICT, strict);
            config.setStrict(strict);
        }
    }

    private String val(EnvironmentVariable variable) {
        return environment.get(variable.toString());
    }

    private Boolean valBool(EnvironmentVariable variable) {
        var value = environment.get(variable.toString());
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }
}
