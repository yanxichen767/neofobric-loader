package dev.neofobric.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 极简事件总线骨架。Forge/NeoForge 用 EventBus 分发游戏事件，Fabric 用 callback。
 * 这里提供统一的中立总线，作为"三合一"在事件层面的公共底座（示意）。
 */
public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        List<Consumer<?>> list = listeners.get(event.getClass());
        if (list == null) return;
        for (Consumer<?> c : list) ((Consumer<T>) c).accept(event);
    }
}
