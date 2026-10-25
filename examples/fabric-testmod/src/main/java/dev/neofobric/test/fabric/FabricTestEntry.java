package dev.neofobric.test.fabric;

/**
 * Fabric 测试 mod 入口。被加载器识别为 FABRIC 类型，经翻译层归一化元数据后实例化。
 *
 * <p>关键演示：构造函数里调用 {@link #selfInit()} 这个 public 实例方法——
 * 该方法在类加载时被 neofobric 的字节码变换注入了一条打印（见 core Transformers），
 * 因此实例化时会看到 "neofobric: fabric_testmod 调用 ...selfInit"。</p>
 */
public final class FabricTestEntry {

    public FabricTestEntry() {
        selfInit(); // 触发被注入的字节码
    }

    public void selfInit() {
        // 原方法体：无任何打印。变换后会被注入一行 neofobric 日志。
    }
}
