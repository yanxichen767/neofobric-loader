package dev.neofobric.core.load;

import dev.neofobric.core.transform.TransformPipeline;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 每个 mod 一个独立的类加载器。核心职责：
 * <ol>
 *   <li><b>隔离</b>：不同 mod 的类不会互相污染（各自从自己 jar 加载）。</li>
 *   <li><b>变换</b>：在类加载时读取原始字节码 → 经 {@link TransformPipeline} 改写 → defineClass。</li>
 * </ol>
 * 这正是 Mixin / NeoForge 在加载期改写类字节码的机制，这里是可独立运行的简化实现。
 */
public final class IsolatedModClassLoader extends URLClassLoader {

    private final String modId;
    private final TransformPipeline pipeline;

    public IsolatedModClassLoader(String modId, URL[] urls, ClassLoader parent, TransformPipeline pipeline) {
        super(urls, parent);
        this.modId = modId;
        this.pipeline = pipeline;
    }

    public String modId() { return modId; }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";
        try (InputStream in = getResourceAsStream(path)) {
            if (in == null) throw new ClassNotFoundException(name);
            byte[] raw = in.readAllBytes();
            // 字节码变换发生在类加载这一刻
            byte[] transformed = pipeline.apply(name, raw);
            return defineClass(name, transformed, 0, transformed.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
