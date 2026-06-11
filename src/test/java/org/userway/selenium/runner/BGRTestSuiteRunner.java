package org.userway.selenium.runner;

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;

import java.time.Duration;

@Suite
@SelectPackages("org.userway.selenium.runner")
public class BGRTestSuiteRunner {

    @BeforeSuite
    static void setup() {
        // Background runner public API is disabled in this version.
        LevelCiBackgroundRunner.getInstance();

        // For clean logs
//        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
    }

    @AfterSuite
    static void teardown() {
        LevelCiBackgroundRunner.getInstance().disableBackgroundRunner();
    }
}
