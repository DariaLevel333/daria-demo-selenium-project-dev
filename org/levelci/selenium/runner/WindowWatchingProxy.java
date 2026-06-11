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
class WindowWatchingProxy implements InvocationHandler {

    private WebDriver.Window window;

    private String driverName;

    private AuditConfig auditConfig;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var methodName = method.getName();
        var result = method.invoke(window, args);

        switch (methodName) {
            case MAXIMIZE_METHOD_NAME:
            case MINIMIZE_METHOD_NAME:
            case FULLSCREEN_METHOD_NAME:
                log.info("Intercepted '#{}()' call on driver {}'s Window. Executing analysis", methodName, driverName);
                SECONDS.sleep(10);
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
            case SET_SIZE_METHOD_NAME:
                log.info("Intercepted '#setSize()' call on driver {}'s Window with args - {}. Executing analysis", driverName, args);
                SECONDS.sleep(10);
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
        }

        return result;
    }

}
