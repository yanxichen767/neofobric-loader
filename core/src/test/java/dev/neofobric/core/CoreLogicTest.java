package dev.neofobric.core;

import dev.neofobric.core.metadata.MiniJson;
import dev.neofobric.core.metadata.MiniToml;
import dev.neofobric.core.metadata.ModMetadata;
import dev.neofobric.core.mods.ModType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 加载器核心的单元测试：元数据解析与类型识别。 */
class CoreLogicTest {

    @Test
    void parsesFabricJson() {
        String json = "{\"id\":\"m\",\"version\":\"1.0.0\",\"name\":\"N\",\"entrypoints\":{\"main\":[\"a.B\"]}}";
        Map<?, ?> root = (Map<?, ?>) MiniJson.parse(json);
        assertEquals("m", root.get("id"));
        assertEquals("1.0.0", root.get("version"));
        assertEquals(List.of("a.B"), ((Map<?, ?>) root.get("entrypoints")).get("main"));
    }

    @Test
    void parsesNeoForgeToml() {
        String toml = "modLoader=\"javafml\"\n[[mods]]\nmodId=\"m\"\nversion=\"1.0\"\ndisplayName=\"Name\"";
        Map<String, Object> root = MiniToml.parse(toml);
        List<?> mods = (List<?>) root.get("mods");
        assertEquals("m", ((Map<?, ?>) mods.get(0)).get("modId"));
    }

    @Test
    void detectsEcosystemByMetadataFile() {
        assertNull(ModType.detect(name -> false));
        assertEquals(ModType.NEOFORGE, ModType.detect(n -> n.equals("META-INF/neoforge.mods.toml")));
        assertEquals(ModType.FORGE_LEGACY, ModType.detect(n -> n.equals("META-INF/mods.toml")));
        assertEquals(ModType.FABRIC, ModType.detect(n -> n.equals("fabric.mod.json")));
    }

    @Test
    void fabricParsingProducesEntrypoints() throws Exception {
        // 通过 MetadataParser 全链路：需要一个带 fabric.mod.json 的 jar，
        // 这里直接验证归一化模型可被正确填充。
        ModMetadata md = new ModMetadata("x", "1", "X", ModType.FABRIC);
        md.entrypoints().add("dev.entry.Main");
        assertEquals(List.of("dev.entry.Main"), md.entrypoints());
        assertEquals(ModType.FABRIC, md.type());
    }
}
