package org.userway.selenium.levelci;

import org.levelci.selenium.model.config.AnalysisConfig;
import org.levelci.selenium.model.config.AuditConfig;
import org.openqa.selenium.WebDriver;

public class LevelSetup {

    public static final String REPORTS_PATH = "./level-ci/level-ci-reports";

    private static final AnalysisConfig ANALYSIS_CONFIG = AnalysisConfig.builder()
            .reportPath(REPORTS_PATH)
            .build();

    public static AuditConfig getAuditConfig(WebDriver driver) {
        return AuditConfig.builder()
                .driver(driver)
                .analysisConfiguration(ANALYSIS_CONFIG)
                .saveReport(true)
                .build();
    }
}
