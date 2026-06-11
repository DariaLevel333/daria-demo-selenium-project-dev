package org.levelci.selenium.runner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.levelci.selenium.AccessibilityAuditor;
import org.levelci.selenium.model.config.AuditConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.levelci.selenium.runner.MethodName.*;
import static org.levelci.selenium.runner.WebDriverWatchingProxy.createWebElementProxy;

@Slf4j
@AllArgsConstructor
class WebElementWatchingProxy implements InvocationHandler {

    private WebElement element;

    private String driverName;

    private AuditConfig auditConfig;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var methodName = method.getName();
        var result = method.invoke(element, args);

        switch (methodName) {
            case FIND_ELEMENT_METHOD_NAME:
                log.info("Intercepted '#findElement()' call on driver {}'s inner element, with args - {}. Creating proxy", driverName, args);
                result = createWebElementProxy(result, driverName, auditConfig);
                break;
            case FIND_ELEMENTS_METHOD_NAME:
                log.info("Intercepted '#findElements()' call on driver {}'s inner element, with args - {}. Creating proxy", driverName, args);
                var castedList = (List<?>) result;
                result = castedList.stream()
                        .map((e) -> createWebElementProxy(e, driverName, auditConfig))
                        .collect(Collectors.toList());
                break;
            case CLICK_METHOD_NAME:
            case SEND_KEYS_METHOD_NAME:
            case CLEAR_METHOD_NAME:
            case SUBMIT_METHOD_NAME:
                log.info("Intercepted '#{}()' call on driver {}'s inner element, with args - {}. Executing analysis", methodName, driverName, args);
                SECONDS.sleep(5);
                AccessibilityAuditor.levelAnalyze(auditConfig);
                break;
        }

        return result;
    }

}
