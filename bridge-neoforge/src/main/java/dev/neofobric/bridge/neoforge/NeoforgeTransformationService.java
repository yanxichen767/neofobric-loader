package dev.neofobric.bridge.neoforge;

import dev.neofobric.bridge.fabric.FabricMetadataConverter;
import dev.neofobric.bridge.fabric.MappingAdapter;
import dev.neofobric.core.discovery.ModScanner;
import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.mods.ModType;

import java.nio.file.Path;
import java.util.List;

/**
 * NeoForge 接入点 —— 把 neofobric 核心挂进真正的游戏加载流程。
 *
 * <p>NeoForge 提供了 {@code net.neoforged.neoforgespi.launch.ITransformationService}
 * 钩子：实现它，就能在模组发现/类变换阶段插入我们自己的逻辑。Sinytra Connector
 * 正是通过实现这个接口来翻译 Fabric mod 的。</p>
 *
 * <p><b>编译说明：</b>本文件依赖 NeoForge 21.1.x 的类，需要 JDK 21 + NeoForge MDK
 * 才能编译；沙箱默认不编译（gradle.properties 中 {@code neofobric.buildNeoforge=false}）。
 * 下面的方法签名以 NeoForge 21.1 的接口为据，集成时请在本地 MDK 内对齐真实签名。</p>
 *
 * <p><b>职责：</b></p>
 * <ol>
 *   <li>在扫描阶段，把 {@code mods} 目录里的 Fabric jar 识别出来；</li>
 *   <li>把它们的元数据转换为 NeoForge 可理解的形态，注册为待加载 mod；</li>
 *   <li>为它们挂上映射适配（MappingAdapter）与变换管线，使按 Yarn 编译的类能正确链接。</li>
 * </ol>
 */
public class NeoforgeTransformationService /* implements net.neoforged.neoforgespi.launch.ITransformationService */ {

    /** 扫描 & 注册 Fabric mod（示意核心逻辑）。 */
    public void registerFabricMods(Path modsDir) throws Exception {
        List<ModCandidate> candidates = ModScanner.scan(modsDir);
        for (ModCandidate c : candidates) {
            if (c.type() != ModType.FABRIC) continue; // 原生 mod 交给 NeoForge 自己加载

            // 1) 元数据转换：fabric.mod.json -> 统一模型
            var md = FabricMetadataConverter.convert(c.jarPath());
            System.out.println("[neofobric] 注册 Fabric mod: " + md);

            // 2) 映射适配：把 yarn/intermediary 名字重映射到 mojang（此处为透传骨架）
            MappingAdapter mapping = MappingAdapter.identity();

            // 3) 交给 NeoForge 变换管线处理该类 jar 的字节码
            registerForTransformation(c, mapping);
        }
    }

    /** 把单个 Fabric jar 及其映射适配交给 NeoForge 的变换服务（示意）。 */
    private void registerForTransformation(ModCandidate c, MappingAdapter mapping) {
        // 真实实现：向 CoreModsTransformer / 变换服务注册一个 ClassTransformer，
        // 内部先执行 mapping.remap，再交给 TransformPipeline。见 core/transform。
        // 这里仅示意调用关系，实际回调在 NeoForge 加载该类时触发。
    }
}
