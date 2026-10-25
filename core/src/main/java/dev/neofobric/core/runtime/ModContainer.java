package dev.neofobric.core.runtime;

import dev.neofobric.core.load.IsolatedModClassLoader;
import dev.neofobric.core.metadata.ModMetadata;

/**
 * 一个已进入运行期的 mod：持有元数据 + 专属类加载器。
 * ModManager 用它来调度入口点、执行生命周期。
 */
public final class ModContainer {
    private final ModMetadata metadata;
    private final IsolatedModClassLoader classLoader;

    public ModContainer(ModMetadata metadata, IsolatedModClassLoader classLoader) {
        this.metadata = metadata;
        this.classLoader = classLoader;
    }

    public ModMetadata metadata()             { return metadata; }
    public IsolatedModClassLoader classLoader() { return classLoader; }

    @Override
    public String toString() { return metadata.toString(); }
}
