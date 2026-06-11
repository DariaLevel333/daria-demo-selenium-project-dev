package org.levelci.selenium.runner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.levelci.selenium.AccessibilityAuditor;
import org.levelci.selenium.model.config.AuditConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.levelci.selenium.runner.MethodName.*;

@Slf4j
@AllArgsConstructor
class NavigationWatchingProxy implements InvocationHandler {

    private WebDriver.Navigation navigation;

    private String driverName;

    private AuditConfig auditConfig;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var methodName = method.getName();
        var result = method.invoke(navigation, args);

        switch (methodName) {
            case BACK_METHOD_NAME:
            case FORWARD_METHOD_NAME:
            case TO_METHOD_NAME:
            case REFRESH_METHOD_NAME:
                log.info("Intercepted '#{}()' call on driver {}'s Navigation, with args - {}. Executing analysis", methodName, driverName, args);
                SECONDS.sleep(10);
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
        }

        return result;
    }
}
