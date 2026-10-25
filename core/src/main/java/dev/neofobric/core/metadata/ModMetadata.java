package dev.neofobric.core.metadata;

import dev.neofobric.core.mods.ModType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 归一化后的 mod 元数据。三套生态（neoforge/forge/fabric）在这里被抽象成同一种模型，
 * 供 runtime 阶段统一使用——这正是"三合一"加载器的价值点之一。
 */
public final class ModMetadata {
    private final String id;
    private final String version;
    private final String displayName;
    private final ModType type;
    /** 统一入口点列表（Forge 系用 {@code @Mod} 类、Fabric 用 entrypoints），按需调度。 */
    private final List<String> entrypoints = new ArrayList<>();
    /** 硬依赖：modId -> 是否必需。 */
    private final Map<String, Boolean> dependencies = new LinkedHashMap<>();

    public ModMetadata(String id, String version, String displayName, ModType type) {
        this.id = id;
        this.version = version;
        this.displayName = displayName;
        this.type = type;
    }

    public String id()          { return id; }
    public String version()     { return version; }
    public String displayName() { return displayName; }
    public ModType type()       { return type; }

    public List<String> entrypoints()          { return entrypoints; }
    public Map<String, Boolean> dependencies() { return dependencies; }

    @Override
    public String toString() {
        return displayName + " (id=" + id + ", v=" + version + ", " + type + ")";
    }
}
