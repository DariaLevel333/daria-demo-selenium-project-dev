package org.levelci.selenium.levelci;

import org.openqa.selenium.JavascriptExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Loads and injects Access Engine + Level CI IIFE (WebdriverIO / Playwright order).
 */
public final class LevelCiScripts {

    private static volatile String accessEngineBody;
    private static volatile String levelCiIifeBody;

    private LevelCiScripts() {}

    public static synchronized String accessEngineSource() throws IOException {
        if (accessEngineBody == null) {
            accessEngineBody = readClasspathResource("static/AccessEngine.professional.js");
        }
        return accessEngineBody;
    }

    public static synchronized String levelCiIifeSource() throws IOException {
        if (levelCiIifeBody == null) {
            levelCiIifeBody = readClasspathResource("static/LevelCI/index.iife.js");
        }
        return levelCiIifeBody;
    }

    private static String readClasspathResource(String path) throws IOException {
        try (var in = LevelCiScripts.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing classpath resource: " + path);
            }
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    public static void injectIfNeeded(JavascriptExecutor js, String accessEngine, String levelCiIife) {
        Boolean ready = (Boolean) js.executeScript(
                "return typeof LevelCiAccessEngine !== 'undefined' && LevelCiAccessEngine != null"
                        + " && typeof LevelCiAccessEngine.runAnalysis === 'function';");
        if (Boolean.TRUE.equals(ready)) {
            return;
        }
        injectScript(js, accessEngine);
        injectScript(js, levelCiIife);
    }

    private static void injectScript(JavascriptExecutor js, String source) {
        js.executeScript(
                "var s = document.createElement('script');"
                        + "s.appendChild(document.createTextNode(arguments[0]));"
                        + "(document.body || document.documentElement).appendChild(s);",
                source);
    }
}
