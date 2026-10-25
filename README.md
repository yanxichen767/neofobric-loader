# neofobric-loader

从零手写的 Minecraft **多生态统一加载器核心源码**（Gradle 多模块工程）。
目标：**NeoForge / Forge 血缘原生直载 + Fabric 经自写翻译层加载**，统一到一个实例。

> 面向 Minecraft **1.21.1**。核心逻辑用纯 Java 11 编写、不依赖 Minecraft，
> 已在沙箱用 JDK21 编译并**真实运行验证通过**（见下"验证结果"）。

---

## 一、为什么是"基座 + 翻译层"而不是"三个加载器拼一起"

Forge 与 NeoForge 是**同一血缘**（NeoForge 2023 年从 Forge 分叉、取代新版本 Forge），
不存在"再挂一个独立 Forge 运行时"的空间；Fabric 是**另一套架构**（不同映射名、不同钩子、
以 Mixin 为主），无法与 Forge 系直接混跑。

因此三合一加载器的工程成立形态只有一种：**一个基座 + 一层翻译**。本工程即按此设计：

- **NeoForge / Forge 血缘 mod** → 原生直载（识别 + 元数据解析 + 隔离加载）
- **Fabric mod** → 由自写翻译层（bridge-fabric）归一化元数据、适配映射后加载

这正是 Sinytra Connector 的思路；本工程把它的核心机制从零写了一遍，作为可学习、可扩展的实现。

---

## 二、模块结构与"从零写"对应关系

```
neofobric-loader/
├── settings.gradle / build.gradle / gradle.properties
├── core/                          # ★ 加载器核心（纯 Java，独立可编译可测）
│   └── dev/neofobric/core/
│       ├── launch/   LauncherMain          统一入口
│       ├── discovery/ModScanner            mod 发现：识别 jar 属于哪个生态
│       ├── metadata/  ModMetadata          三种生态的统一元数据模型
│       │             MetadataParser        TOML(forge/neoforge) + JSON(fabric) 归一化解析
│       │             MiniJson / MiniToml   自写的最小 JSON/TOML 解析器（零第三方依赖）
│       ├── transform/ ClassTransformer     字节码变换接口
│       │             TransformPipeline     变换管线（顺序敏感）
│       │             Transformers          内置示例：给方法注入打印（用 ASM）
│       ├── load/     IsolatedModClassLoader 每 mod 独立类加载器 + 加载期变换
│       ├── runtime/  ModContainer/ModManager 容器管理、依赖检查、入口调度
│       └── event/    EventBus              中立事件总线（Forge/Fabric 公共底座示意）
├── bridge-fabric/                   # ★ Fabric 翻译层（从零写 Connector 的核心）
│   │   FabricMetadataConverter     fabric.mod.json → 统一模型
│   │   MappingAdapter              映射适配接口（yarn/intermediary → mojang）
├── bridge-neoforge/                 # NeoForge 接入点（需 JDK21 + NeoForge MDK 编译）
│   │   NeoforgeTransformationService  实现 ITransformationService，把核心挂进游戏
├── examples/
│   ├── fabric-testmod/              Fabric 测试 mod（fabric.mod.json）
│   └── neoforge-testmod/            NeoForge 测试 mod（neoforge.mods.toml）
└── verification/                    # 本地复验脚本（verify.sh）+ 测试 jar + ASM
```

**每部分对应真实生态的哪个机制：**
| 本工程模块 | 对应真实机制 |
| --- | --- |
| ModScanner / ModType | Fabric Loader 与 FML 各自的 mod 发现 |
| MetadataParser | 各自的 metadata 解析（fabric.mod.json / neoforge.mods.toml） |
| TransformPipeline + Transformers | Mixin / NeoForge 的加载期字节码变换 |
| IsolatedModClassLoader | 各类加载器的类加载隔离与变换挂点 |
| FabricMetadataConverter + MappingAdapter | Sinytra Connector 的元数据转换 + 重映射 |

---

## 三、构建与运行

### 方式 A：Gradle（标准，推荐）
需要 JDK 11+（编核心）或 JDK 21（编 NeoForge 接入层）。

```bash
cd neofobric-loader
gradle :core:build          # 编译核心 + 运行 JUnit（core 自带的单测）
gradle :examples:fabric-testmod:jar
gradle :examples:neoforge-testmod:jar
```

> NeoForge 接入层默认关闭（`gradle.properties` 里 `neofobric.buildNeoforge=false`），
> 因为需要 JDK21 + NeoForge MDK 依赖。要在本地开启接入层集成，再打开该开关并按
> `bridge-neoforge/build.gradle` 里的说明配置 moddev 插件。

### 方式 B：快速验证核心（不依赖 Gradle）
```bash
./verification/verify.sh     # 需要 javac + java
```
脚本会：编译 core → 用两个测试 mod jar 跑 LauncherMain → 跑断言 VerifyMain。

### 直接运行加载器入口
```bash
java -cp "core/构建产物:asm库" dev.neofobric.core.launch.LauncherMain <mods目录>
```

---

## 四、验证结果（沙箱实测，JDK21 编译、Java11 目标）

```
发现 2 个 mod:
  · NEOFORGE: 1 个 → 原生直载
  · FABRIC:   1 个 → 经翻译层(Fabric)
已解析并登记: 2 个
== 启动入口点（字节码变换随类加载发生）==
neofobric: fabric_testmod 调用 dev.neofobric.test.fabric.FabricTestEntry.selfInit
== 加载完成 ==
  [FABRIC]   Neofobric Fabric Test Mod (id=fabric_testmod,  v=1.0.0, FABRIC)
  [NEOFORGE] Neofobric NeoForge Test Mod (id=neoforge_testmod, v=1.0.0, NEOFORGE)
== 验证结果: PASS (Fabric=true, NeoForge=true, 登记数=2) ==
```

其中 `neofobric: fabric_testmod 调用 ...selfInit` 这行，原方法体是空的、没有任何打印——
它是加载器在**类加载时**用 ASM 注入的字节码产生的，证明变换管线真实生效。

---

## 五、诚实边界（读完再继续扩展）

1. **这是加载器的"核"与"骨架"，不是开箱即用的生产加载器。** 它验证了原理与架构，
   但要在真实 Minecraft 1.21.1 里跑，还需把 `bridge-neoforge` 接入层与本地 NeoForge MDK
   对齐签名并编译。
2. **`MappingAdapter` 目前是透传骨架。** 真正让 Fabric mod 在 NeoForge 上链接，需要一份
   yarn/intermediary → mojang 的官方映射表并用 TinyMappings 之类加载——这是 Connector
   工作量最大的部分，属于数据工程，超出单文件范围。
3. **Forge 血缘的入口发现**（`@Mod` 注解扫描）在接入层完成，核心里简化了。
4. **不是"三个加载器拼一起"**：工程成立形态是"NeoForge 基座 + 自写 Fabric 翻译层"。
