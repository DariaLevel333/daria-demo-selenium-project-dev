package org.levelci.selenium.runner;

import lombok.extern.slf4j.Slf4j;

/*
 * BGR user-facing API imports (kept as comments while the public Background
 * Runner API is disabled). Restore alongside the commented methods below.
 *
 * import org.openqa.selenium.*;
 * import org.openqa.selenium.bidi.HasBiDi;
 * import org.openqa.selenium.chromium.*;
 * import org.openqa.selenium.devtools.HasDevTools;
 * import org.openqa.selenium.federatedcredentialmanagement.HasFederatedCredentialManagement;
 * import org.openqa.selenium.html5.LocationContext;
 * import org.openqa.selenium.html5.WebStorage;
 * import org.openqa.selenium.interactions.Interactive;
 * import org.openqa.selenium.logging.HasLogEvents;
 * import org.openqa.selenium.mobile.NetworkConnection;
 * import org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator;
 * import org.levelci.selenium.model.config.AuditConfig;
 *
 * import java.lang.reflect.Proxy;
 */

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Background Runner is currently disabled from public/user use.
 *
 * The user-facing API ({@code setGlobalAuditConfig}, {@code enableBackgroundRunner},
 * {@code watchDriver}, {@code watchDriverImpl}) is commented out below. The
 * lifecycle methods {@code getInstance()}, {@code isBackgroundRunnerActive()} and
 * {@code disableBackgroundRunner()} are intentionally kept so internal callers
 * (e.g. {@code AccessibilityAuditor}) continue to compile; with the enable path
 * commented out, {@code isBackgroundRunnerActive()} will always return {@code false}.
 */
@Slf4j
public class LevelCiBackgroundRunner {

    private static LevelCiBackgroundRunner INSTANCE;

    // private AuditConfig auditConfig; // disabled while BGR public API is commented out

    private final AtomicBoolean isActive = new AtomicBoolean(false);

    /*
    public void setGlobalAuditConfig(AuditConfig auditConfig) {
        if (isActive.get()) {
            throw new IllegalStateException("Cannot override global AuditConfig because background runner is already active");
        }
        this.auditConfig = auditConfig;
    }

    public void enableBackgroundRunner() {
        this.isActive.set(true);
    }
    */

    public void disableBackgroundRunner() {
        this.isActive.set(false);
    }

    public boolean isBackgroundRunnerActive() {
        return this.isActive.get();
    }

    public static LevelCiBackgroundRunner getInstance() {
        return INSTANCE == null ? INSTANCE = new LevelCiBackgroundRunner() : INSTANCE;
    }

    /*
    public <D extends WebDriver> D watchDriver(D driver) {
        return this.watchDriverImpl(driver, driver.toString());
    }

    public <D extends WebDriver> D watchDriver(D driver, String customId) {
        return this.watchDriverImpl(driver, customId);
    }

    @SuppressWarnings("unchecked")
    private <D extends WebDriver> D watchDriverImpl(D driver, String customId) {
        if (driver == null || driver.toString() == null) {
            throw new IllegalStateException("Cannot watch driver because driver is null or was already closed (driver.toString() == null)");
        }
        if (driver instanceof ChromiumDriver) {
            return (D) Proxy.newProxyInstance(
                    driver.getClass().getClassLoader(),
                    new Class<?>[] {
                            WebDriver.class,
                            JavascriptExecutor.class,
                            HasCapabilities.class,
                            HasFederatedCredentialManagement.class,
                            HasVirtualAuthenticator.class,
                            Interactive.class,
                            PrintsPage.class,
                            TakesScreenshot.class,
                            HasAuthentication.class, // Not common for FF, Safari and Chromium
                            HasBiDi.class, // Not common for FF, Safari and Chromium
                            HasCasting.class, // Not common for FF, Safari and Chromium
                            HasCdp.class, // Not common for FF, Safari and Chromium
                            HasDevTools.class, // Not common for FF, Safari and Chromium
                            HasLaunchApp.class, // Not common for FF, Safari and Chromium
                            HasLogEvents.class, // Not common for FF, Safari and Chromium
                            HasNetworkConditions.class, // Not common for FF, Safari and Chromium
                            HasPermissions.class, // Not common for FF, Safari and Chromium
                            LocationContext.class, // Not common for FF, Safari and Chromium
                            NetworkConnection.class, // Not common for FF, Safari and Chromium
                            WebStorage.class // Not common for FF, Safari and Chromium
                    },
                    new WebDriverWatchingProxy<>(
                            customId,
                            driver,
                            AuditConfig.deepCopy(auditConfig)
                    )
            );
        }
        return (D) Proxy.newProxyInstance(
                driver.getClass().getClassLoader(),
                new Class<?>[] {
                        WebDriver.class,
                        JavascriptExecutor.class,
                        HasCapabilities.class,
                        HasFederatedCredentialManagement.class,
                        HasVirtualAuthenticator.class,
                        Interactive.class,
                        PrintsPage.class,
                        TakesScreenshot.class,
                },
                new WebDriverWatchingProxy<>(
                        customId,
                        driver,
                        AuditConfig.deepCopy(auditConfig)
                )
        );
    }
    */

    private LevelCiBackgroundRunner() {}

}
