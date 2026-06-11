package org.levelci.selenium.runner;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.levelci.selenium.AccessibilityAuditor;
import org.levelci.selenium.model.config.AuditConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.levelci.selenium.runner.MethodName.*;

@Slf4j
class WebDriverWatchingProxy<D extends WebDriver> implements InvocationHandler {

    private final String driverName;

    private final D driver;

    private final AuditConfig auditConfig;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var methodName = method.getName();
        var result = method.invoke(driver, args);

        switch (methodName) {
            case GET_METHOD_NAME:
            case EXECUTE_SCRIPT_METHOD_NAME:
                log.info("Intercepted '#{}()' call on driver {}, with args - {}. Executing analysis", methodName, driverName, args);
                SECONDS.sleep(10); // Heuristic waiting
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
            case EXECUTE_ASYNC_SCRIPT_METHOD_NAME:
                log.info("Intercepted '#executeAsyncScript()' call on driver {}, with args - {}. Executing analysis", driverName, args);
                SECONDS.sleep(20); // Heuristic waiting
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
            case NAVIGATE_METHOD_NAME:
                log.info("Intercepted '#navigate()' call on driver {}. Creating proxy", driverName);
                result = createNavigationProxy(result, driverName, auditConfig);
                break;
            case MANAGE_METHOD_NAME:
                log.info("Intercepted '#manage()' call on driver {}. Creating proxy", driverName);
                result = createOptionsProxy(result, driverName, auditConfig);
                break;
            case FIND_ELEMENT_METHOD_NAME:
                log.info("Intercepted '#findElement()' call on driver {}, with args - {}. Creating proxy", driverName, args);
                result = createWebElementProxy(result, driverName, auditConfig);
                break;
            case FIND_ELEMENTS_METHOD_NAME:
                log.info("Intercepted '#findElements()' call on driver {}, with args - {}. Creating proxy", driverName, args);
                var castedList = (List<?>) result;
                result = castedList.stream()
                        .map((e) -> createWebElementProxy(e, driverName, auditConfig))
                        .collect(Collectors.toList());
                break;
        }

        return result;
    }

    static Object createWebElementProxy(Object element, String driverName, AuditConfig auditConfig) {
        if (element == null) {
            return null;
        }
        return Proxy.newProxyInstance(
                element.getClass().getClassLoader(),
                new Class<?>[] {
                    WebElement.class,
                    SearchContext.class,
                    TakesScreenshot.class
                },
                new WebElementWatchingProxy(
                        (WebElement) element,
                        driverName,
                        auditConfig
                )
        );
    }

    static Object createNavigationProxy(Object navigation, String driverName, AuditConfig auditConfig) {
        if (navigation == null) {
            return null;
        }
        return Proxy.newProxyInstance(
                navigation.getClass().getClassLoader(),
                new Class<?>[] { WebDriver.Navigation.class },
                new NavigationWatchingProxy(
                        (WebDriver.Navigation) navigation,
                        driverName,
                        auditConfig
                )
        );
    }

    static Object createOptionsProxy(Object options, String driverName, AuditConfig auditConfig) {
        if (options == null) {
            return null;
        }
        return Proxy.newProxyInstance(
            options.getClass().getClassLoader(),
            new Class<?>[] { WebDriver.Options.class },
                new OptionsWatchingProxy(
                        (WebDriver.Options) options,
                        driverName,
                        auditConfig
                )
        );
    }

    WebDriverWatchingProxy(String driverName, D driver, AuditConfig auditConfig) {
        this.driverName = driverName;
        this.driver = driver;

        auditConfig.setDriver(driver);
        auditConfig.setSaveReport(true);
        auditConfig.setBackgroundRunnerMode(true);

        this.auditConfig = auditConfig;
    }

}
