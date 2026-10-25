package dev.neofobric.core.discovery;

import dev.neofobric.core.mods.ModCandidate;
import dev.neofobric.core.mods.ModType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * mod 发现器：扫描 mods 目录，识别每个 jar 的生态类型。
 * 识别原则：以 jar 内「存在哪种元数据文件」为准，而不是看后缀/名字，
 * 因为三类 mod 都是 .jar。library/非 mod jar 会被跳过。
 */
public final class ModScanner {

    private ModScanner() {}

    /** 扫描目录下所有 jar，返回被识别的 mod 候选。 */
    public static List<ModCandidate> scan(Path modsDir) throws IOException {
        List<ModCandidate> out = new ArrayList<>();
        if (!Files.isDirectory(modsDir)) return out;
        try (Stream<Path> s = Files.list(modsDir)) {
            for (Path p : s.filter(f -> f.getFileName().toString().endsWith(".jar")).collect(Collectors.toList())) {
                ModType type = detectType(p);
                if (type != null) out.add(new ModCandidate(p, type));
            }
        }
        return out;
    }

    /** 判定单个 jar 的生态类型；不是 mod（或不可识别）返回 null。 */
    public static ModType detectType(Path jar) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            return ModType.detect(name -> jf.getJarEntry(name) != null);
        } catch (IOException e) {
            return null; // 打不开的 jar 视为无效，跳过
        }
    }
}
