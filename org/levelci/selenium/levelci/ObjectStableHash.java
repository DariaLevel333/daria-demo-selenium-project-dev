package org.levelci.selenium.levelci;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Port of {@code buildObjectStableHash} from {@code @level-ci/a11y-app-shared} / object-hasher.ts
 * for cross-tool compatible scope and issue hashes.
 */
public final class ObjectStableHash {

    private ObjectStableHash() {}

    public static String buildObjectStableHash(JsonNode obj) {
        return buildObjectStableHash(obj, Collections.emptySet());
    }

    public static String buildObjectStableHash(JsonNode obj, Set<String> omittedKeys) {
        if (obj == null || !obj.isObject()) {
            throw new IllegalArgumentException("Expected JSON object");
        }
        ObjectNode o = (ObjectNode) obj;
        List<String> keys = new ArrayList<>();
        o.fieldNames().forEachRemaining(keys::add);
        keys.sort(String::compareTo);
        keys.removeIf(omittedKeys::contains);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            String key = keys.get(i);
            String shortKey = key.length() >= 3 ? key.substring(0, 3) : key;
            sb.append(shortKey).append(':').append(buildValueHash(o.get(key)));
        }
        return md5Base64Url(sb.toString());
    }

    private static String buildValueHash(JsonNode value) {
        if (value == null || value.isNull()) {
            return "null";
        }
        if (value.isBoolean()) {
            throw new IllegalArgumentException("Unsupported JSON boolean in stable hash input");
        }
        if (value.isNumber()) {
            if (value.isIntegralNumber()) {
                return String.valueOf(value.longValue());
            }
            return String.valueOf(value.doubleValue());
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isArray()) {
            ArrayNode a = (ArrayNode) value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(buildValueHash(a.get(i)));
            }
            return sb.toString();
        }
        if (value.isObject()) {
            return buildObjectStableHash(value);
        }
        throw new IllegalArgumentException("Unsupported JSON node type: " + value.getNodeType());
    }

    private static String md5Base64Url(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
