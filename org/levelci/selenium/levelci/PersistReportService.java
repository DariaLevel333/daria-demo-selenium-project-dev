package org.levelci.selenium.levelci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.levelci.selenium.utils.Constants.MAPPER;

/**
 * Port of {@code persistReport} from {@code @level-ci/a11y-app-shared}.
 */
public final class PersistReportService {

    private static final Set<String> ISSUE_HASH_OMIT = Set.of("screenshotPath");

    private PersistReportService() {}

    public static synchronized void persistReport(String outputDir, JsonNode report) throws IOException {
        JsonNode meta = report.get("meta");
        JsonNode rulesNode = report.get("rules");
        if (meta == null || !meta.isObject()) {
            throw new IllegalArgumentException("report.meta must be an object");
        }
        ArrayNode rules;
        if (rulesNode == null || rulesNode.isNull()) {
            rules = MAPPER.createArrayNode();
        } else if (rulesNode.isArray()) {
            rules = (ArrayNode) rulesNode;
        } else {
            throw new IllegalArgumentException("report.rules must be an array");
        }

        Path output = Path.of(outputDir);
        Files.createDirectories(output);
        Path baseDir = output.resolve("scope-reports");
        Files.createDirectories(baseDir);

        ObjectNode scopeReportMetaBase = MAPPER.createObjectNode();
        scopeReportMetaBase.set("type", meta.get("type"));
        scopeReportMetaBase.set("version", meta.get("version"));
        ObjectNode envMinimal = MAPPER.createObjectNode();
        envMinimal.set("url", meta.get("env").get("url"));
        scopeReportMetaBase.set("env", envMinimal);

        String scopeHash = ObjectStableHash.buildObjectStableHash(scopeReportMetaBase);
        ObjectNode scopeReportMeta = scopeReportMetaBase.deepCopy();
        scopeReportMeta.put("hash", scopeHash);

        Path scopeDirPath = baseDir.resolve(scopeHash);
        Files.createDirectories(scopeDirPath);

        Path scopeMetaFilePath = scopeDirPath.resolve("scope-meta.json");
        if (!Files.exists(scopeMetaFilePath)) {
            Files.writeString(scopeMetaFilePath, MAPPER.writeValueAsString(scopeReportMeta), StandardCharsets.UTF_8);
        }

        Path cachesDirPath = scopeDirPath.resolve(".cache");
        Path issuesDirPath = scopeDirPath.resolve("issues");
        Path linksDirPath = scopeDirPath.resolve("analysis-links");
        Files.createDirectories(cachesDirPath);
        Files.createDirectories(issuesDirPath);
        Files.createDirectories(linksDirPath);

        ObjectNode analysisMeta = buildAnalysisMeta((ObjectNode) meta);
        Path analysisMetaPath = scopeDirPath.resolve("analysis-meta.ndjson");
        Files.writeString(
                analysisMetaPath,
                MAPPER.writeValueAsString(analysisMeta) + "\n",
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND);

        for (JsonNode rule : rules) {
            String ruleId = rule.get("ruleId").asText();
            Path ruleCachePath = cachesDirPath.resolve(ruleId + ".json");
            Path ruleIssuesPath = issuesDirPath.resolve(ruleId + ".ndjson");
            Path ruleLinksPath = linksDirPath.resolve(ruleId + ".ndjson");

            RuleCache cache = RuleCache.parse(readFileOrEmpty(ruleCachePath));
            StringBuilder issuesBlock = new StringBuilder();
            StringBuilder linksBlock = new StringBuilder();

            JsonNode issues = rule.get("issues");
            if (issues != null && issues.isArray()) {
                for (JsonNode analysisIssue : issues) {
                    if (!analysisIssue.isObject()) {
                        continue;
                    }
                    ObjectNode scopeIssue = buildScopeReportIssue((ObjectNode) analysisIssue);
                    String issueHash = scopeIssue.get("hash").asText();
                    boolean added = cache.add(issueHash);
                    if (added) {
                        issuesBlock.append(MAPPER.writeValueAsString(scopeIssue)).append('\n');
                    }
                    ObjectNode link = MAPPER.createObjectNode();
                    link.put("metaHash", analysisMeta.get("hash").asText());
                    link.put("issueHash", issueHash);
                    linksBlock.append(MAPPER.writeValueAsString(link)).append('\n');
                }
            }

            if (issuesBlock.length() > 0) {
                Files.writeString(
                        ruleIssuesPath,
                        issuesBlock.toString(),
                        StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            }
            if (linksBlock.length() > 0) {
                Files.writeString(
                        ruleLinksPath,
                        linksBlock.toString(),
                        StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            }
            Files.writeString(ruleCachePath, cache.serialize(), StandardCharsets.UTF_8);
        }
    }

    private static String readFileOrEmpty(Path path) throws IOException {
        if (!Files.exists(path)) {
            return "";
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static ObjectNode buildAnalysisMeta(ObjectNode meta) {
        String envHash = ObjectStableHash.buildObjectStableHash((ObjectNode) meta.get("env"));
        ObjectNode copy = meta.deepCopy();
        copy.put("hash", envHash);
        return copy;
    }

    private static ObjectNode buildScopeReportIssue(ObjectNode analysisIssue) {
        String hash = ObjectStableHash.buildObjectStableHash(analysisIssue, ISSUE_HASH_OMIT);
        ObjectNode out = analysisIssue.deepCopy();
        out.put("hash", hash);
        return out;
    }

    private static final class RuleCache {
        private final Map<String, java.util.List<String>> baseObject = new HashMap<>();

        static RuleCache parse(String data) throws IOException {
            RuleCache c = new RuleCache();
            if (data == null || data.isBlank()) {
                return c;
            }
            JsonNode n = MAPPER.readTree(data);
            if (!n.isObject()) {
                return c;
            }
            n.fields().forEachRemaining(e -> {
                java.util.List<String> list = new java.util.ArrayList<>();
                if (e.getValue().isArray()) {
                    for (JsonNode x : e.getValue()) {
                        list.add(x.asText());
                    }
                }
                c.baseObject.put(e.getKey(), list);
            });
            return c;
        }

        String serialize() throws IOException {
            ObjectNode o = MAPPER.createObjectNode();
            for (Map.Entry<String, java.util.List<String>> e : baseObject.entrySet()) {
                var arr = o.putArray(e.getKey());
                for (String v : e.getValue()) {
                    arr.add(v);
                }
            }
            return MAPPER.writeValueAsString(o);
        }

        boolean add(String hash) {
            String hashKey = hash.substring(0, 2);
            java.util.List<String> values = baseObject.computeIfAbsent(hashKey, k -> new java.util.ArrayList<>());
            if (values.contains(hash)) {
                return false;
            }
            values.add(hash);
            return true;
        }
    }
}
