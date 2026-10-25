package dev.neofobric.core.mods;

/**
 * 生态类型。加载器按此分派：原生直载 vs 翻译层。
 *
 * <p>三合一加载器的关键：NeoForge 与 Forge 是同一血缘（Forge 在新版本被 NeoForge 取代），
 * 因此它们走「原生直载」；Fabric 是另一套架构，走「翻译层」。这也正是本工程
 * 相对 Sinytra Connector 的定位：基座统一、翻译层自写。</p>
 */
public enum ModType {
    /** 原生 NeoForge mod（META-INF/neoforge.mods.toml）——直接加载。 */
    NEOFORGE("META-INF/neoforge.mods.toml", true),
    /** 老 Forge / Forge 血缘 mod（META-INF/mods.toml），1.20.1 及更早与 NeoForge 通用——直接加载。 */
    FORGE_LEGACY("META-INF/mods.toml", true),
    /** Fabric / Quilt mod（fabric.mod.json）——需经翻译层（bridge-fabric）。 */
    FABRIC("fabric.mod.json", false);

    /** 该类型在 jar 内的元数据文件名。 */
    public final String metadataFile;
    /** 是否由核心直接加载（true=原生直载；false=需要翻译层适配）。 */
    public final boolean nativeLoad;

    ModType(String metadataFile, boolean nativeLoad) {
        this.metadataFile = metadataFile;
        this.nativeLoad = nativeLoad;
    }

    /** 按 jar 内存在的元数据文件名反查类型。 */
    public static ModType detect(java.util.function.Predicate<String> hasEntry) {
        if (hasEntry.test(NEOFORGE.metadataFile)) return NEOFORGE;
        if (hasEntry.test(FORGE_LEGACY.metadataFile)) return FORGE_LEGACY;
        if (hasEntry.test(FABRIC.metadataFile)) return FABRIC;
        return null; // 既非 mod 或类型不可识别
    }
}
