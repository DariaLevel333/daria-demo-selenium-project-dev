package org.levelci.selenium.runner;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.levelci.selenium.model.config.AuditConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import static org.levelci.selenium.runner.MethodName.WINDOW_METHOD_NAME;

@Slf4j
@AllArgsConstructor
class OptionsWatchingProxy implements InvocationHandler {

    private WebDriver.Options options;

    private String driverName;

    private AuditConfig auditConfig;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var methodName = method.getName();
        var result = method.invoke(options, args);

        if (Objects.equals(methodName, WINDOW_METHOD_NAME)) {
            log.info("Intercepted '#window()' call on driver {}'s Options. Creating proxy", driverName);
            result = createWindowProxy(result, driverName, auditConfig);
        }

        return result;
    }

    static Object createWindowProxy(Object window, String driverName, AuditConfig auditConfig) {
        if (window == null) {
            return null;
        }
        return Proxy.newProxyInstance(
                window.getClass().getClassLoader(),
                new Class<?>[] { WebDriver.Window.class },
                new WindowWatchingProxy(
                        (WebDriver.Window) window,
                        driverName,
                        auditConfig
                )
        );
    }
}
