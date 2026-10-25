package dev.neofobric.core.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * 内置示例变换器：给类的每个 public 实例方法入口注入一行打印。
 *
 * <p>它证明了字节码变换在类加载时真正发生：原本没有打印的类，经本变换器改写后，
 * 运行时会输出 "neofobric: &lt;modId&gt; 调用 xxx.yyy"。这等价于 Mixin / NeoForge
 * 在加载期改写 vanilla 类的机制（简化版）。</p>
 */
public final class Transformers {

    private Transformers() {}

    /** 构造一个在指定 modId 的所有 public 方法入口注入打印的变换器。 */
    public static ClassTransformer injectLoadingLog(String modId) {
        return (className, original) -> {
            try {
                ClassNode node = new ClassNode();
                new ClassReader(original).accept(node, 0);
                boolean changed = false;

                for (MethodNode mn : node.methods) {
                    if (!isInjectable(mn)) continue;
                    // 在该方法第一条指令之前插入: System.out.println("neofobric: <modId> 调用 <类名>.<方法名>")
                    AbstractInsnNode first = mn.instructions.getFirst();
                    InsnList inject = new InsnList();
                    inject.add(new FieldInsnNode(Opcodes.GETSTATIC,
                            "java/lang/System", "out", "Ljava/io/PrintStream;"));
                    inject.add(new LdcInsnNode("neofobric: " + modId + " 调用 "
                            + className.replace('/', '.') + "." + mn.name));
                    inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                            "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
                    mn.instructions.insert(first, inject);
                    changed = true;
                }

                if (!changed) return original;
                // COMPUTE_MAXS：注入字节码改变了栈深度，让 ASM 重算 maxStack，避免 VerifyError
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(cw);
                return cw.toByteArray();
            } catch (RuntimeException e) {
                // 变换失败就保留原字节码，不让单个 mod 拖垮整个加载器
                return original;
            }
        };
    }

    private static boolean isInjectable(MethodNode mn) {
        return (mn.access & Opcodes.ACC_PUBLIC) != 0
                && (mn.access & Opcodes.ACC_STATIC) == 0
                && !mn.name.equals("<init>")
                && !mn.name.equals("<clinit>");
    }
}
