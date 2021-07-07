/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package client.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;

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
    public boolean post(Event event, IEventBusInvokeDispatcher wrapper) {
        return false;
    }

    @Override
    public void shutdown() {}

    @Override
    public void start() {}
}
