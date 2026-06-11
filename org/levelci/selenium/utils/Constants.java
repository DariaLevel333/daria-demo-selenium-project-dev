package org.levelci.selenium.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Constants {

    public static final String A11Y_RESULTS_DEFAULT_FOLDER_PATH = "./level-ci-reports";

    public static final String FRAMEWORK_NAME = "Selenium Java";

    public static final String REPORT_RULE_ID = "ruleId";

    public static final String GET_INNER_HEIGHT_SCRIPT = "return window.innerHeight";
    public static final String GET_INNER_WIDTH_SCRIPT = "return window.innerWidth";

    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        var module = new SimpleModule();
        MAPPER.registerModule(module);
    }
}
