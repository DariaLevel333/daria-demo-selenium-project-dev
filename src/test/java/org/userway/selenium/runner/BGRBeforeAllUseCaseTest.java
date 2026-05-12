package org.userway.selenium.runner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BGRBeforeAllUseCaseTest {

    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        var options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));

        // Get proxy instance
        driver = LevelCiBackgroundRunner.getInstance().watchDriver(driver, "BGRBeforeAllUseCase-shared-driver");
    }

    @AfterAll
    static void teardown() {
        driver.close();
    }

    @Test
    void test_1() throws MalformedURLException {
        driver.get("https://userway.org");
        driver.navigate().to("https://userway.org/widget/");
        driver.navigate().to(new URL("https://userway.org/accessibility-monitor/"));
        driver.navigate().back();
        driver.navigate().forward();
    }

    @Test
    void test_2() {
        driver.get("https://userway.org/press/");
        driver.navigate().refresh();
        driver.get("https://userway.org/ci-cd/");
        driver.manage().window().minimize();
        driver.manage().window().maximize();
        driver.manage().window().fullscreen();
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    @Test
    void test_3() {
        driver.get("https://scan.userway.org");
        var element = driver.findElement(By.cssSelector("html"));
        element.click();
        element.click();
        element.click();
        element.sendKeys("afj;asdjfakas;f");
    }

    @Test
    void test_4() {
        driver.get("https://userway.org/pdf/");
        driver.get("https://userway.org/");
        driver.findElement(By.cssSelector("html"))
                .findElement(By.cssSelector("body"))
                .findElements(By.cssSelector("button"))
                .forEach((e) -> {
                    try {
                        e.click();
                    } catch (Exception ignored) {}
                });
    }

}
