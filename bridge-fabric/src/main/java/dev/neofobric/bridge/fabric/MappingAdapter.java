package dev.neofobric.bridge.fabric;

/**
 * 映射适配接口 —— 三合一加载器最硬的一块。
 *
 * <p>Fabric mod 按 <b>Intermediary / Yarn</b> 命名编译，NeoForge 世界按 <b>Mojang 官方名</b>。
 * 字节码里的方法/字段名对不上，类就链接失败。Connector 的核心工作量就在这里：
 * 维护一套 yarn→mojang 的重映射，在类加载时把名字换过去。</p>
 *
 * <p>本接口定义重映射的契约：输入一个类名，输出重映射后的字节码。
 * 完整实现需要一份「官方映射表」（可从 Mojang 官方映射 / yarn 仓库生成），
 * 属于数据工程 + 大量维护，远超单文件范围。这里给出骨架与接入点，
 * 用真实数据填充 {@code remap} 即可让 Fabric 类在 NeoForge 上正确链接。</p>
 */
@FunctionalInterface
public interface MappingAdapter {

    /** 对给定类的字节码做名字空间重映射（Intermediary/Yarn → Mojang 官方名）。 */
    byte[] remap(String className, byte[] original);

    /**
     * 空实现：仅透传。接入真实映射表后替换。
     * 可借助 TinyMappings 库加载 yarn/tiny 映射文件。
     */
    static MappingAdapter identity() {
        return (name, data) -> data;
    }
}
