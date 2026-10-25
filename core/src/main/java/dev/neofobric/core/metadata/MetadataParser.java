package dev.neofobric.core.metadata;

import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.mods.ModType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * 从 mod jar 读取元数据，并按生态类型分派到对应解析器，统一归一化为 {@link ModMetadata}。
 *
 * <p>这是"识别"阶段的核心：一个 jar 进来，先由 {@link ModType#detect} 判定是
 * NeoForge / Forge / Fabric，再走各自的元数据格式解析。</p>
 */
public final class MetadataParser {

    private MetadataParser() {}

    /** 读取并解析一个候选 mod 的元数据。jar 必须已通过 ModType#detect 识别类型。 */
    public static ModMetadata parse(ModCandidate candidate) throws IOException {
        try (JarFile jar = new JarFile(candidate.jarPath().toFile())) {
            String content = readEntry(jar, candidate.type().metadataFile);
            switch (candidate.type()) {
                case NEOFORGE:
                case FORGE_LEGACY: return parseToml(candidate.type(), content);
                case FABRIC:       return parseFabric(candidate.type(), content);
                default: throw new IOException("不支持的 mod 类型: " + candidate.type());
            }
        }
    }

    private static String readEntry(JarFile jar, String name) throws IOException {
        var entry = jar.getJarEntry(name);
        if (entry == null) throw new IOException("jar 中缺少元数据: " + name);
        try (var in = jar.getInputStream(entry)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Forge / NeoForge 血缘：TOML。取 [[mods]] 首项。 */
    private static ModMetadata parseToml(ModType type, String content) throws IOException {
        Map<String, Object> root = MiniToml.parse(content);
        List<?> mods = (List<?>) root.get("mods");
        if (mods == null || mods.isEmpty()) throw new IOException("TOML 缺少 [[mods]] 段: " + type);
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) mods.get(0);
        String id = str(first.get("modId"));
        if (id == null) throw new IOException("缺少 modId");
        ModMetadata m = new ModMetadata(id,
                str(first.get("version")),
                str(first.get("displayName")) != null ? str(first.get("displayName")) : id,
                type);
        // 可选：主入口类（Forge 系通常用注解 @Mod，此处不强求）
        return m;
    }

    /** Fabric 系：JSON。读 id/version/name + entrypoints。 */
    private static ModMetadata parseFabric(ModType type, String content) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) MiniJson.parse(content);
        String id = str(root.get("id"));
        if (id == null) throw new IOException("fabric.mod.json 缺少 id");
        ModMetadata m = new ModMetadata(id,
                str(root.get("version")),
                str(root.get("name")) != null ? str(root.get("name")) : id,
                type);
        // 收集各阶段 entrypoint 类（main/client/server/realm 等）
        Object ep = root.get("entrypoints");
        if (ep instanceof Map) {
            for (Object v : ((Map<?, ?>) ep).values()) {
                if (v instanceof List) for (Object e : (List<?>) v) addClass(m, e);
                else addClass(m, v);
            }
        }
        return m;
    }

    private static void addClass(ModMetadata m, Object cls) {
        if (cls instanceof String) m.entrypoints().add((String) cls);
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    /** 供 bridge 层在内存中构造 Fabric 元数据使用。 */
    public static String readRaw(Path jarPath, String entry) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            return readEntry(jar, entry);
        }
    }
}
