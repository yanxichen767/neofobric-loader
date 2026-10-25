package dev.neofobric.core.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自包含的最小 TOML 解析器（不引第三方依赖）。用于解析 neoforge.mods.toml / mods.toml。
 * 支持：注释、key=value（字符串/数字/布尔/内联数组）、[section]、[[array-of-tables]]。
 * 足够覆盖两种 Forge 血缘元数据文件的常用字段。
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class MiniToml {

    private MiniToml() {}

    /** 解析 TOML 文本，返回嵌套 Map。根表键都放在返回值的顶层；[[arr]] 得到 List&lt;Map&gt;。 */
    public static Map<String, Object> parse(String text) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> current = root;

        for (String raw : text.split("\n")) {
            String line = stripComment(raw).trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("[[") && line.endsWith("]]")) {
                // array-of-tables：取或建数组，push 新表
                String key = line.substring(2, line.length() - 2).trim();
                List<Map<String, Object>> arr = tableArray(root, key);
                Map<String, Object> item = new LinkedHashMap<>();
                arr.add(item);
                current = item;
            } else if (line.startsWith("[") && line.endsWith("]")) {
                String key = line.substring(1, line.length() - 1).trim();
                current = table(root, key);
            } else {
                int eq = line.indexOf('=');
                if (eq < 0) throw new IllegalArgumentException("TOML 非法行: " + line);
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                current.put(k, parseValue(v));
            }
        }
        return root;
    }

    private static String stripComment(String line) {
        // 简单起见：忽略行内 #（不含引号内场景；元数据文件足够简单）
        int idx = line.indexOf('#');
        return idx < 0 ? line : line.substring(0, idx);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> tableArray(Map<String, Object> root, String key) {
        Object o = root.get(key);
        if (o == null) {
            List<Map<String, Object>> l = new ArrayList<>();
            root.put(key, l);
            return l;
        }
        return (List<Map<String, Object>>) o;
    }

    private static Map<String, Object> table(Map<String, Object> root, String key) {
        Object o = root.get(key);
        if (o instanceof Map) return (Map<String, Object>) o;
        Map<String, Object> m = new LinkedHashMap<>();
        root.put(key, m);
        return m;
    }

    private static Object parseValue(String v) {
        if (v.startsWith("[")) { // 内联数组
            List<Object> list = new ArrayList<>();
            String inner = v.substring(1, v.length() - 1).trim();
            if (!inner.isEmpty()) {
                for (String item : inner.split(",")) {
                    list.add(parseScalar(item.trim()));
                }
            }
            return list;
        }
        return parseScalar(v);
    }

    private static Object parseScalar(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).replace("\\\"", "\"");
        }
        if (s.equals("true")) return Boolean.TRUE;
        if (s.equals("false")) return Boolean.FALSE;
        return Double.parseDouble(s); // 版本/数字兜底
    }
}
