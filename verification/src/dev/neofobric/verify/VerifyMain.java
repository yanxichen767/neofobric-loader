package dev.neofobric.verify;

import dev.neofobric.core.discovery.ModScanner;
import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.mods.ModType;
import dev.neofobric.core.runtime.ModManager;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 集成验证入口：真实跑一遍加载器核心，并做断言。
 * <pre>java -cp "out/core:libs/*:out/verify" dev.neofobric.verify.VerifyMain &lt;mods目录&gt;</pre>
 */
public final class VerifyMain {

    public static void main(String[] args) throws Exception {
        Path modsDir = Path.of(args[0]);
        System.out.println("[verify] 扫描 " + modsDir);

        // 1) 发现 & 类型识别
        List<ModCandidate> candidates = ModScanner.scan(modsDir);
        Map<ModType, Long> byType = candidates.stream()
                .collect(Collectors.groupingBy(ModCandidate::type, Collectors.counting()));
        System.out.println("[verify] 识别结果: " + byType);

        boolean foundFabric  = byType.containsKey(ModType.FABRIC);
        boolean foundNeoForge = byType.containsKey(ModType.NEOFORGE);

        // 2) 解析 & 登记 & 加载 & 触发入口（触发字节码变换）
        ModManager manager = new ModManager(VerifyMain.class.getClassLoader());
        int n = manager.initialize(candidates);
        System.out.println("[verify] 已登记 " + n + " 个 mod");
        manager.launch();

        // 3) 断言
        boolean pass = foundFabric && foundNeoForge && n == candidates.size();
        System.out.println("== 验证结果: " + (pass ? "PASS" : "FAIL")
                + " (Fabric=" + foundFabric + ", NeoForge=" + foundNeoForge + ", 登记数=" + n + ") ==");
        System.exit(pass ? 0 : 1);
    }
}
