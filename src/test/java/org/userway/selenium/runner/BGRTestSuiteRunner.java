package org.userway.selenium.runner;

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.levelci.selenium.model.config.AnalysisConfig;
import org.levelci.selenium.model.config.AuditConfig;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;

import java.time.Duration;

@Suite
@SelectPackages("org.userway.selenium.runner")
public class BGRTestSuiteRunner {

    @BeforeSuite
    static void setup() {
        var backgroundRunner = LevelCiBackgroundRunner.getInstance();
        backgroundRunner.setGlobalAuditConfig(
                AuditConfig.builder()
                        .strict(false)
                        .auditTimeout(Duration.ofMinutes(20))
                        .analysisConfiguration(
                                AnalysisConfig.builder()
                                        .reportPath("./level-ci/level-ci-reports")
                                        .build()
                        )
                        .saveReport(true)
                        .build()
        );
        backgroundRunner.enableBackgroundRunner();

        // For clean logs
//        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
    }

    @AfterSuite
    static void teardown() {
        LevelCiBackgroundRunner.getInstance().disableBackgroundRunner();
    }
}
