package dev.neofobric.core.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自包含的最小 JSON 解析器（不引第三方依赖，保证 core 可独立编译）。
 * 支持对象/数组/字符串/数字/布尔/null。用于解析 fabric.mod.json。
 */
public final class MiniJson {
    private final String s;
    private int i;

    private MiniJson(String s) { this.s = s; }

    /** 解析一段 JSON 文本，返回 Object：Map / List / String / Double / Boolean / null。 */
    public static Object parse(String text) {
        MiniJson p = new MiniJson(text);
        Object v = p.value();
        p.skipWs();
        if (p.i < p.s.length()) throw new IllegalArgumentException("JSON 尾部存在多余内容: " + p.s.substring(p.i));
        return v;
    }

    private Object value() {
        skipWs();
        char c = peek();
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (c == 't' || c == 'f') return bool();
        if (c == 'n') { i += 4; return null; }
        return number();
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; skipWs();
        if (peek() == '}') { i++; return m; }
        while (true) {
            skipWs();
            if (peek() != '"') throw new IllegalArgumentException("JSON 对象键必须是字符串");
            String k = string();
            skipWs();
            expect(':');
            m.put(k, value());
            skipWs();
            char c = peek();
            if (c == ',') { i++; continue; }
            if (c == '}') { i++; return m; }
            throw new IllegalArgumentException("JSON 对象缺逗号/右花括号");
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++; skipWs();
        if (peek() == ']') { i++; return l; }
        while (true) {
            l.add(value());
            skipWs();
            char c = peek();
            if (c == ',') { i++; continue; }
            if (c == ']') { i++; return l; }
            throw new IllegalArgumentException("JSON 数组缺逗号/右方括号");
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++; // 开引号
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case '/': case '\\': case '"': sb.append(e); break;
                    case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: throw new IllegalArgumentException("非法转义: \\" + e);
                }
            } else sb.append(c);
        }
        throw new IllegalArgumentException("JSON 字符串未闭合");
    }

    private Object bool() {
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        throw new IllegalArgumentException("非法布尔");
    }

    private Object number() {
        int start = i;
        while (i < s.length() && "0123456789+-.eE".indexOf(s.charAt(i)) >= 0) i++;
        return Double.parseDouble(s.substring(start, i));
    }

    private void skipWs() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
    private char peek() { return s.charAt(i); }
    private void expect(char c) { if (s.charAt(i++) != c) throw new IllegalArgumentException("期望字符: " + c); }
}
