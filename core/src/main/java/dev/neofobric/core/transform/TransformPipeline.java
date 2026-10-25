package dev.neofobric.core.transform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 变换管线：把一个 mod 的若干个变换器串起来，按顺序作用于每个类的字节码。
 * 顺序敏感：先做基础改写、再做 API 桥接/注入，后置变换依赖前置的结果。
 */
public final class TransformPipeline {
    private final List<ClassTransformer> transformers = new CopyOnWriteArrayList<>();

    public TransformPipeline add(ClassTransformer t) { transformers.add(t); return this; }
    public List<ClassTransformer> transformers() { return transformers; }

    public byte[] apply(String className, byte[] original) {
        byte[] data = original;
        for (ClassTransformer t : transformers) data = t.transform(className, data);
        return data;
    }
}
