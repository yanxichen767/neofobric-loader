package dev.neofobric.core.mods;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 一个被发现的 mod 候选：指向 jar 文件 + 已识别出的生态类型。
 * 发现阶段只做「识别」，不加载类；真正加载在 runtime 阶段。
 */
public final class ModCandidate {
    private final Path jarPath;
    private final ModType type;
    private final URL[] classpath;

    public ModCandidate(Path jarPath, ModType type) {
        this.jarPath = Objects.requireNonNull(jarPath);
        this.type = Objects.requireNonNull(type);
        try {
            this.classpath = new URL[]{jarPath.toUri().toURL()};
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("非法 mod 路径: " + jarPath, e);
        }
    }

    public Path jarPath()   { return jarPath; }
    public ModType type()   { return type; }
    public URL[] classpath() { return classpath; }

    @Override
    public String toString() {
        return jarPath.getFileName() + " [" + type + "]";
    }
}
