package io.github.soir20.moremcmeta.eventbus;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A fake event bus that tracks the addition and removal of listeners. Only the register() and unregister()
 * methods are implemented; the rest simply fulfill {@link IEventBus}.
 * @author soir20
 */
public class MockEventBus implements IEventBus {
    private final ArrayList<Object> LISTENERS;

    public MockEventBus() {
        LISTENERS = new ArrayList<>();
    }

    @Override
    public void register(Object target) {
        LISTENERS.add(target);
    }

    @Override
    public void unregister(Object object) {
        LISTENERS.remove(object);
    }

    public List<Object> getListeners() {
        return LISTENERS;
    }

    public void clear() {
        LISTENERS.clear();
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {}

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {}

    @Override
    public <T extends Event> void
    addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> consumer) {}

    @Override
    public <T extends Event> void
    addListener(EventPriority priority, boolean receiveCancelled, Class<T> eventType, Consumer<T> consumer) {}

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
                                                                            Consumer<T> consumer) {

    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
                                                                            EventPriority priority,
                                                                            Consumer<T> consumer) {

    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
                                                                            EventPriority priority,
                                                                            boolean receiveCancelled,
                                                                            Consumer<T> consumer) {

    }

    @Override
    public <T extends GenericEvent<? extends F>, F> void addGenericListener(Class<F> genericClassFilter,
                                                                            EventPriority priority,
                                                                            boolean receiveCancelled,
                                                                            Class<T> eventType,
                                                                            Consumer<T> consumer) {

    }

    @Override
    public boolean post(Event event) {
        return false;
    }

    @Override
    public void shutdown() {}

    @Override
    public void start() {}
}
