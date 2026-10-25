package dev.neofobric.core.launch;

import dev.neofobric.core.discovery.ModScanner;
import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.mods.ModType;
import dev.neofobric.core.runtime.ModManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 加载器统一入口（可独立运行，不依赖 Minecraft）。
 *
 * <p>演示完整链路：扫描 mods 目录 → 识别生态类型 → 解析元数据 → 建立隔离类加载器
 * → 字节码变换 → 实例化入口点。NeoForge 接入层（bridge-neoforge）会把同样的核心
 * 以 ITransformationService 挂进真正的游戏环境。</p>
 *
 * <pre>用法: java -cp "core/target/classes:libs/*" dev.neofobric.core.launch.LauncherMain &lt;mods目录&gt;</pre>
 */
public final class LauncherMain {

    public static void main(String[] args) throws IOException {
        Path modsDir = args.length > 0 ? Path.of(args[0]) : Path.of("mods");
        System.out.println("== neofobric 加载器 ==");
        System.out.println("扫描目录: " + modsDir.toAbsolutePath());

        List<ModCandidate> candidates = ModScanner.scan(modsDir);
        System.out.println("发现 " + candidates.size() + " 个 mod:");
        Map<ModType, Long> byType = candidates.stream()
                .collect(Collectors.groupingBy(ModCandidate::type, Collectors.counting()));
        for (ModType t : ModType.values()) {
            long n = byType.getOrDefault(t, 0L);
            String path = t.nativeLoad ? "原生直载" : "经翻译层(Fabric)";
            System.out.println("  · " + t + ": " + n + " 个 → " + path);
        }

        ModManager manager = new ModManager(LauncherMain.class.getClassLoader());
        int registered = manager.initialize(candidates);
        System.out.println("已解析并登记: " + registered + " 个");

        List<String> missing = manager.missingDependencies();
        if (!missing.isEmpty()) {
            System.out.println("[警告] 依赖缺失:");
            missing.forEach(m -> System.out.println("  - " + m));
        }

        System.out.println("== 启动入口点（字节码变换随类加载发生）==");
        manager.launch();

        System.out.println("== 加载完成 ==");
        manager.containers().forEach((id, c) ->
                System.out.println("  [" + c.metadata().type() + "] " + c));
    }
}
