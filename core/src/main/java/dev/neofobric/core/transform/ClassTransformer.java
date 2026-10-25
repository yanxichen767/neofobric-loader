package dev.neofobric.core.transform;

/**
 * 字节码变换器：输入一个类的原始字节码，输出变换后的字节码。
 * 与 Mixin / NeoForge / Connector 的变换管线的核心概念一致——
 * 加载器在类加载时对每个类的字节做就地改写。
 */
@FunctionalInterface
public interface ClassTransformer {
    /** @return 变换后的字节码（可返回原数组表示不变）。 */
    byte[] transform(String className, byte[] original);
}
