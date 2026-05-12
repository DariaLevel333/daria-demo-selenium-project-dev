package org.userway.selenium.runner;

import org.junit.jupiter.api.Test;
import org.levelci.selenium.runner.LevelCiBackgroundRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class BGRInlineUseCaseTest {

    @Test
    void test_1() {
        WebDriver driver = new ChromeDriver(
                new ChromeOptions().addArguments("--headless")
        );
        driver = LevelCiBackgroundRunner.getInstance()
                .watchDriver(driver, "BGRInlineUseCase-test_1");
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));
        // Each will be ignored
        driver.get("https://userway.org/");
        driver.get("https://userway.org");
    }

    @Test
    void test_2() {
        WebDriver driver = new ChromeDriver(
                new ChromeOptions().addArguments("--headless")
        );
        driver = LevelCiBackgroundRunner.getInstance()
                .watchDriver(driver, "BGRInlineUseCase-test_1");
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));

        driver.get("https://userway.org/testimonials/");
        driver.get("https://userway.org/blog/");
    }

    @Test
    void test_3() {
        WebDriver driver = new ChromeDriver(
                new ChromeOptions().addArguments("--headless")
        );
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));
        driver = LevelCiBackgroundRunner.getInstance()
                .watchDriver(driver, "BGRInlineUseCase-test_1");

        driver.get("https://userway.org/university/");
        driver.get("https://userway.org/faq/");
    }

}
