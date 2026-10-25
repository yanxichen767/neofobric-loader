#!/usr/bin/env bash
# 本地复验脚本：需要 javac（JDK 11+）。
# 用法: ./verification/verify.sh   （会自动编译并运行，见 README）
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
V="$ROOT/verification"
JC="${JC:-javac}"
JAVA="${JAVA:-java}"
LIBS="$V/libs/asm-9.7.jar:$V/libs/asm-tree-9.7.jar"
OUT="$V/out"
rm -rf "$OUT/core" "$OUT/verify"; mkdir -p "$OUT/core" "$OUT/verify"
find "$ROOT/core/src/main/java" -name '*.java' > "$V/core-sources.txt"
"$JC" -encoding UTF-8 --release 11 -cp "$LIBS" -d "$OUT/core" @"$V/core-sources.txt"
"$JC" -encoding UTF-8 --release 11 -cp "$OUT/core:$LIBS" -d "$OUT/verify" "$V/src/dev/neofobric/verify/VerifyMain.java"
echo "== 集成验证(LauncherMain) =="
"$JAVA" -cp "$OUT/core:$LIBS" dev.neofobric.core.launch.LauncherMain "$OUT/mods"
echo "== 断言验证(VerifyMain) =="
"$JAVA" -cp "$OUT/verify:$OUT/core:$LIBS" dev.neofobric.verify.VerifyMain "$OUT/mods"
echo "退出码: $?"
