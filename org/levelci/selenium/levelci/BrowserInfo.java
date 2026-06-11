package org.levelci.selenium.levelci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of {@code getBrowserInfo} from {@code @level-ci/a11y-app-shared}.
 */
public final class BrowserInfo {

    private final String browser;
    private final String browserVersion;

    private BrowserInfo(String browser, String browserVersion) {
        this.browser = browser;
        this.browserVersion = browserVersion;
    }

    public String browser() {
        return browser;
    }

    public String browserVersion() {
        return browserVersion;
    }

    @FunctionalInterface
    private interface Condition {
        boolean test(String userAgent);
    }

    private static final class Entry {
        final String name;
        final Pattern pattern;
        final Condition condition;

        Entry(String name, Pattern pattern, Condition condition) {
            this.name = name;
            this.pattern = pattern;
            this.condition = condition;
        }
    }

    private static final Entry[] ENTRIES = {
            new Entry("Electron", Pattern.compile("Electron/(\\d+(\\.\\d+)?)"), ua -> ua.contains("Electron")),
            new Entry("Firefox", Pattern.compile("Firefox/(\\d+(\\.\\d+)?)"), ua -> ua.contains("Firefox")),
            new Entry("Edge", Pattern.compile("Edg/(\\d+(\\.\\d+)?)"), ua -> ua.contains("Edg")),
            new Entry("HeadlessChrome", Pattern.compile("HeadlessChrome/(\\d+(\\.\\d+)?)"),
                    ua -> ua.contains("HeadlessChrome")),
            new Entry("Chrome", Pattern.compile("Chrome/(\\d+(\\.\\d+)?)"),
                    ua -> ua.contains("Chrome") && ua.contains("Safari") && !ua.contains("Edg")),
            new Entry("Safari", Pattern.compile("Version/(\\d+(\\.\\d+)?)"),
                    ua -> ua.contains("Safari") && !ua.contains("Chrome")),
    };

    public static BrowserInfo fromUserAgent(String userAgent) {
        if (userAgent == null) {
            return new BrowserInfo("Unknown Browser", "Unknown Version");
        }
        for (Entry e : ENTRIES) {
            if (!e.condition.test(userAgent)) {
                continue;
            }
            Matcher m = e.pattern.matcher(userAgent);
            if (m.find()) {
                return new BrowserInfo(e.name, m.group(1));
            }
        }
        return new BrowserInfo("Unknown Browser", "Unknown Version");
    }
}
