package org.levelci.selenium.levelci;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Maven-filtered {@code integration.version} from {@code library.properties}.
 */
public final class LibraryInfo {

    private static final String FALLBACK = "0.0.15";
    private static final String INTEGRATION_VERSION = load();

    private LibraryInfo() {}

    public static String integrationVersion() {
        return INTEGRATION_VERSION;
    }

    private static String load() {
        try (InputStream in = LibraryInfo.class.getResourceAsStream("/org/levelci/selenium/library.properties")) {
            if (in == null) {
                return FALLBACK;
            }
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("integration.version", FALLBACK);
        } catch (IOException e) {
            return FALLBACK;
        }
    }
}
