# Level CI — Java Selenium sample

Sample project for **Level CI** accessibility analysis with Selenium. Two modes: **manual** scans and **background runner** (proxied `WebDriver`).

## Adapter JAR (vendored)

The Level CI Java adapter **`a11y-selenium-java`** is checked in under **`lib/`** (currently **`lib/a11y-selenium-java-0.0.15.jar`**). The sample **`pom.xml`** wires it with **`system`** scope and **`systemPath`**, and declares its runtime dependencies explicitly (Selenium, Jackson, Commons IO, SLF4J) so the test classpath matches what the adapter needs.

**Selenium** is pinned to **4.14.1** to align with that JAR.

To refresh the JAR after building the library from source (from your `web-accessibility-java-selenium` checkout):

```bash
mvn -q package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
cp target/a11y-selenium-java-0.0.15.jar /path/to/a11y-selenium-java-sample/lib/
```

If the library version changes, rename the file under **`lib/`** and update **`systemPath`** (and dependency **`version`**) in **`pom.xml`**, and align the explicit dependency versions with the library’s **`pom.xml`**.

The Maven coordinates for the adapter remain **`org.userway:a11y-selenium-java`** (published groupId). Java types live under **`org.userway.selenium.*`** in that JAR.

---

## Dependencies (snippet)

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.14.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.userway</groupId>
    <artifactId>a11y-selenium-java</artifactId>
    <version>0.0.15</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/a11y-selenium-java-0.0.15.jar</systemPath>
</dependency>
<!-- plus jackson-databind, commons-io, slf4j-api, selenium-chrome-driver — see pom.xml -->
```

## 1. Manual mode

Prepare analysis configuration and run the scan. Example: package **`org.userway.selenium.manual`** (paths match the JAR).

Configure Surefire to **exclude** background-runner tests under **`org.userway.selenium.runner`**:

```xml
<configuration>
    <excludes>
      <exclude>**/org/userway/selenium/runner/*.java</exclude>
    </excludes>
</configuration>
```

Run manual tests only:

```shell
mvn clean test
```

## 2. Background runner mode

Use a **Level CI** proxy `WebDriver` from the background runner API:

```java
var myDriver = new ChromeDriver(); // or FirefoxDriver, SafariDriver, etc.
//rename
var bgRunner = UserWayBackgroundRunner.getInstance();

myDriver = bgRunner.watchDriver(myDriver, "BGRInlineUseCase-test_1");
```

The proxy runs Level CI analysis when the page changes (e.g. after `get(...)`).

Configure the runner **before** tests and tear it down **after** (often with a JUnit **`@Suite`**). Add **`junit-platform-suite`** if you use suites.

Example suite selection: **`@SelectPackages("org.userway.selenium.runner")`**.

- **`@BeforeSuite`**: `UserWayBackgroundRunner.getInstance()`, set global audit config, `enableBackgroundRunner()`.
- **`@AfterSuite`**: `disableBackgroundRunner()`.

To run **only** background-runner tests, Surefire should **include** the suite class, e.g. **`BGRTestSuiteRunner`**.

```xml
<configuration>
    <includes>
        <include>**/BGRTestSuiteRunner.java</include>
    </includes>
</configuration>
```

```shell
mvn clean test
```

---

Logging uses **Logback** for SLF4J; adjust levels in **`src/test/resources/logback.xml`**.
