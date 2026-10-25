package dev.neofobric.core.runtime;

import dev.neofobric.core.load.IsolatedModClassLoader;
import dev.neofobric.core.metadata.ModMetadata;
import dev.neofobric.core.metadata.MetadataParser;
import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.transform.TransformPipeline;
import dev.neofobric.core.transform.Transformers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * mod 管理器：把「候选」升级为「运行容器」。
 * <ol>
 *   <li>解析元数据；</li>
 *   <li>为每个 mod 建立隔离类加载器 + 变换管线（默认注入加载日志，演示字节码变换）；</li>
 *   <li>依赖完整性检查；</li>
 *   <li>launch：实例化各入口点，触发生命周期。</li>
 * </ol>
 */
public final class ModManager {
    private final Map<String, ModContainer> containers = new LinkedHashMap<>();
    private final ClassLoader parent;

    public ModManager(ClassLoader parent) { this.parent = parent; }

    /** 解析并登记所有候选。返回已登记数量。 */
    public int initialize(List<ModCandidate> candidates) throws IOException {
        for (ModCandidate c : candidates) {
            ModMetadata md = MetadataParser.parse(c);
            TransformPipeline pipeline = new TransformPipeline()
                    .add(Transformers.injectLoadingLog(md.id()));
            IsolatedModClassLoader loader = new IsolatedModClassLoader(
                    md.id(), c.classpath(), parent, pipeline);
            containers.put(md.id(), new ModContainer(md, loader));
        }
        return containers.size();
    }

    /** 依赖检查：硬依赖必须存在（忽略对 minecraft / java 的内建依赖）。 */
    public List<String> missingDependencies() {
        List<String> missing = new ArrayList<>();
        for (ModContainer c : containers.values()) {
            for (Map.Entry<String, Boolean> dep : c.metadata().dependencies().entrySet()) {
                String id = dep.getKey();
                boolean required = dep.getValue();
                if (!required) continue;
                if ("minecraft".equals(id) || "java".equals(id)) continue;
                if (!containers.containsKey(id)) missing.add(c.metadata().id() + " 缺少依赖: " + id);
            }
        }
        return missing;
    }

    /** 实例化每个 mod 的入口点类（触发其加载即触发字节码变换，构造即触发生命周期）。 */
    public void launch() {
        for (ModContainer c : containers.values()) {
            for (String entry : c.metadata().entrypoints()) {
                try {
                    Class<?> cls = Class.forName(entry, true, c.classLoader());
                    cls.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("入口点初始化失败: " + entry + " <- " + c.metadata().id(), e);
                }
            }
        }
    }

    public Map<String, ModContainer> containers() { return containers; }
}
