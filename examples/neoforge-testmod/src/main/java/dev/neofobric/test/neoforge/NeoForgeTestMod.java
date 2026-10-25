package dev.neofobric.test.neoforge;

/**
 * NeoForge 测试 mod 入口。
 *
 * <p>真实 NeoForge 系 mod 用 {@code @Mod("modid")} 注解标记入口类，由 NeoForge 的
 * 注解扫描发现；在本加载器核心的简化实现里，Forge/NeoForge 血缘 mod 由 {@code [[mods]]}
 * 元数据识别并解析，入口发现交给接入层（bridge-neoforge）在真实环境完成。</p>
 */
// @net.neoforged.fml.common.Mod("neoforge_testmod")
public final class NeoForgeTestMod {

    public NeoForgeTestMod() {
        // 真实环境由 NeoForge 构造并调度生命周期
    }

    public void onCommonSetup() {
        // 占位：真实游戏事件回调处
    }
}
