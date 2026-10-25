package dev.neofobric.bridge.fabric;

import dev.neofobric.core.metadata.MetadataParser;
import dev.neofobric.core.metadata.ModMetadata;
import dev.neofobric.core.mods.ModType;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Fabric 翻译层 —— 元数据转换。
 *
 * <p>三合一加载器里，Fabric mod 不能直接交给 NeoForge 原生加载，因为两者的
 * 元数据格式和加载契约都不同。这一步把 fabric.mod.json 归一化成加载器统一模型，
 * 供上层（NeoForge 接入层）注册成一个"看起来像原生 mod"的条目。</p>
 *
 * <p>真实 Connector 的做法与此一致：{@code FabricModMetadataParser} 把 Fabric 元数据
 * 转换为 NeoForge 的 {@code ModInfo} 形态，再注入变换服务。</p>
 */
public final class FabricMetadataConverter {

    private FabricMetadataConverter() {}

    /**
     * 读取 fabric.mod.json 并归一化为统一元数据。
     * 这里复用 core 的 {@link MetadataParser}（它已支持 FABRIC 分支），
     * 返回的对象即被接入层注册。
     */
    public static ModMetadata convert(Path fabricJar) throws IOException {
        ModMetadata md = MetadataParser.parse(
                new dev.neofobric.core.mods.ModCandidate(fabricJar, ModType.FABRIC));
        return md;
    }
}
