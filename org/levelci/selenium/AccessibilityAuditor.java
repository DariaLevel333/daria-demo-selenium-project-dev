package org.levelci.selenium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.levelci.selenium.levelci.AnalysisReportFactory;
import org.levelci.selenium.levelci.LevelCiScripts;
import org.levelci.selenium.levelci.PersistReportService;
import org.levelci.selenium.levelci.RunAnalysisArgsFactory;
import org.levelci.selenium.model.AnalysisResult;
import org.levelci.selenium.model.config.AnalysisConfig;
import org.levelci.selenium.model.config.AuditConfig;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;

import java.io.IOException;
import java.util.Map;

import static org.levelci.selenium.utils.Constants.*;

/**
 * Selenium-based Level CI accessibility analysis using Level Access Engine + {@code LevelCiAccessEngine}.
 */
@Slf4j
public class AccessibilityAuditor {

    /**
     * Runs access-engine analysis on the current page and optionally persists Level CI scope reports.
     *
     * @param config audit configuration (requires non-null {@code AuditConfig.driver})
     * @return {@link AnalysisResult} with optional {@link org.levelci.selenium.model.AnalysisReport},
     *         optional {@link org.levelci.selenium.model.AnalysisError}, and effective {@link AnalysisConfig}
     */
    public static AnalysisResult levelAnalyze(AuditConfig config) {
        if (config.getDriver() == null) {
            log.error("AuditConfig.driver is required");
            return AnalysisResult.failed();
        }
        var driver = config.getDriver();
        var url = driver.getCurrentUrl();

        log.info("Starting Level CI analysis for {}", url);

        var beginMillis = System.currentTimeMillis();

        if (config.getAnalysisConfiguration() == null) {
            config.setAnalysisConfiguration(AnalysisConfig.builder().build());
        }

        var analysisConfig = config.getAnalysisConfiguration();
        if (Boolean.TRUE.equals(analysisConfig.getSwitchOff())) {
            log.warn("Environment property LEVEL_CI_SWITCH_OFF (or config switchOff) has value 'true'. Analysis will not be executed");
            var endMillis = System.currentTimeMillis();
            log.info("Analysis for {} finished in {} seconds", url, (endMillis - beginMillis) / 1000.0);
            return AnalysisResult.didNotRun(analysisConfig);
        }

        log.debug("Overriding configuration with values from environment");
        var environmentService = new EnvironmentService(System.getenv());
        environmentService.overrideWithEnvironmentValues(config);
        log.debug("Configuration was supplemented with environment variables");

        analysisConfig = config.getAnalysisConfiguration();
        String reportPath = analysisConfig.getReportPath();
        String destDirPath =
                reportPath == null || reportPath.isBlank() ? A11Y_RESULTS_DEFAULT_FOLDER_PATH : reportPath;

        log.debug("Loading Level Access + Level CI scripts");
        final String accessEngine;
        final String levelCi;
        try {
            accessEngine = LevelCiScripts.accessEngineSource();
            levelCi = LevelCiScripts.levelCiIifeSource();
        } catch (IOException e) {
            log.error("Could not load access engine scripts", e);
            return finishFailed(url, beginMillis, analysisConfig);
        }

        log.debug("Creating report directories");
        if (!IOService.prepareFilesystem(destDirPath)) {
            log.error("Could not create directories for reports. Shutting down analysis");
            return finishFailed(url, beginMillis, analysisConfig);
        }

        driver.manage().timeouts().scriptTimeout(config.getAuditTimeout());
        var jsExec = (JavascriptExecutor) driver;
        driver.switchTo().defaultContent();

        try {
            LevelCiScripts.injectIfNeeded(jsExec, accessEngine, levelCi);
        } catch (Exception e) {
            log.error("Script injection failed", e);
            return finishFailed(url, beginMillis, analysisConfig);
        }

        log.info("Executing LevelCiAccessEngine.runAnalysis on {}", url);
        @SuppressWarnings("unchecked")
        Map<String, Object> runArgs = MAPPER.convertValue(RunAnalysisArgsFactory.build(analysisConfig), Map.class);
        final String rawJson;
        try {
            rawJson = (String) jsExec.executeScript(
                    "return JSON.stringify(LevelCiAccessEngine.runAnalysis(arguments[0]));",
                    runArgs);
        } catch (Exception e) {
            log.error("runAnalysis failed", e);
            return finishFailed(url, beginMillis, analysisConfig);
        }
        log.info("runAnalysis completed for {}", url);

        JsonNode runResult;
        try {
            runResult = MAPPER.readTree(rawJson);
        } catch (JsonProcessingException e) {
            log.error("Analysis response has unknown format", e);
            return finishFailed(url, beginMillis, analysisConfig);
        }

        JsonNode rules = runResult.get("rules");
        if (config.isStrict() && rules != null && rules.size() > 0) {
            log.error("Analysis failed on strict ({} rules with findings)", rules.size());
            var endMillis = System.currentTimeMillis();
            log.info("Analysis for {} finished in {} seconds", url, (endMillis - beginMillis) / 1000.0);
            var engineReport = StatisticsService.parseEngineReport(runResult);
            return AnalysisResult.failedOnStrict(engineReport, analysisConfig);
        }

        String userAgent = readUserAgent(jsExec);
        int vw = intFromScript(jsExec, GET_INNER_WIDTH_SCRIPT);
        int vh = intFromScript(jsExec, GET_INNER_HEIGHT_SCRIPT);

        JsonNode fullReport = AnalysisReportFactory.buildReport(runResult, config, userAgent, vw, vh);

        var isBackgroundRunnerActive = LevelCiBackgroundRunner.getInstance().isBackgroundRunnerActive();
        AnalysisResult statistics;
        if (!isBackgroundRunnerActive) {
            statistics = StatisticsService.succeededFromRunResult(runResult, analysisConfig);
        } else {
            statistics = AnalysisResult.didNotRun(analysisConfig);
        }

        if (config.isSaveReport()) {
            log.info("Persisting Level CI scope report under {}", destDirPath);
            try {
                PersistReportService.persistReport(destDirPath, fullReport);
            } catch (IOException e) {
                log.error("persistReport failed", e);
            }
        }

        var endMillis = System.currentTimeMillis();
        log.info("Analysis for {} finished in {} seconds", url, (endMillis - beginMillis) / 1000.0);
        return statistics;
    }

    private static AnalysisResult finishFailed(String url, long beginMillis, AnalysisConfig analysisConfig) {
        var endMillis = System.currentTimeMillis();
        log.info("Analysis for {} finished in {} seconds", url, (endMillis - beginMillis) / 1000.0);
        return AnalysisResult.failed(analysisConfig);
    }

    private static String readUserAgent(JavascriptExecutor js) {
        try {
            return String.valueOf(js.executeScript("return navigator.userAgent;"));
        } catch (Exception e) {
            return "";
        }
    }

    private static int intFromScript(JavascriptExecutor js, String script) {
        try {
            Object o = js.executeScript(script);
            if (o instanceof Number) {
                return ((Number) o).intValue();
            }
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }
}
