package org.userway.selenium.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class BGRBeforeEachUseCaseTest {

    private WebDriver driver;

    private static int testNumber = 1;

    @BeforeEach
    void setUp() {
        var options = new ChromeOptions().addArguments("--headless");
        this.driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));

        // Get proxy instance
        driver = LevelCiBackgroundRunner.getInstance()
                .watchDriver(driver, "BGRBeforeEachUseCaseTest-test_" + (testNumber++) + "-driver");
    }

    @AfterEach
    void teardown() {
        this.driver.close();
    }

    @Test
    void test_1() {
        driver.get("https://userway.org");
        driver.get("https://userway.org/pricing/#accessibilitywidget");
        driver.manage().timeouts().scriptTimeout(Duration.ofMinutes(50));
        var res = ((JavascriptExecutor) driver).executeAsyncScript("for (let i = 0; i < 500; ++i) {\n" +
                "    console.log(999999999 * 999999999);\n" +
                "}\n" +
                "var callback = arguments[arguments. length - 1];\n" +
                "callback('aaaaaaaaaaaaaaa');\n");
        assertThat(res).isEqualTo("aaaaaaaaaaaaaaa");
    }

    @Test
    void test_2() {
        driver.get("https://userway.org/pricing/#accessibilitywidgetbundles");
        driver.get("https://userway.org/pricing/#monitor");
        ((JavascriptExecutor) driver).executeScript("" +
                "for (let i = 0; i < 5000; ++i) {\n" +
                "    console.log(999999999 * 999999999);\n" +
                "}\n");
    }

    @Test
    void test_3() {
        driver.get("https://userway.org/pricing/#accessibilitywidgetbundles");
        driver.get("https://userway.org/pricing/#audits");
        driver.get("https://userway.org/pricing/#pdf");
        driver.get("https://userway.org/pricing/#enterprise");
        driver.get("https://userway.org/pricing/#custombundles");
        driver.get("https://userway.org/pricing/#accessibilitywidget");
    }

    @Test
    void test_4() {
        driver.get("https://userway.org/partner/");
    }

}
