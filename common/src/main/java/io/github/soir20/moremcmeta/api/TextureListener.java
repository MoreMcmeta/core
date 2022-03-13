/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.api;

import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A listener for an {@link EventDrivenTexture}'s events.
 * @author soir20
 */
public class TextureListener<V> {

    /**
     * The available event types for listeners.
     */
    public enum Type {
        REGISTRATION,
        BIND,
        UPLOAD,
        TICK,
        CLOSE,
    }

    private final Type TYPE;
    private final Consumer<? super V> ACTION;

    /**
     * Creates a listener for a certain type of event.
     * @param type      event type
     * @param action    callback to execute when the event occurs
     */
    public TextureListener(Type type, Consumer<? super V> action) {
        TYPE = requireNonNull(type, "Type cannot be null");
        ACTION = requireNonNull(action, "Action cannot be null");
    }

    /**
     * Gets this listener's event type.
     * @return the event type of this listener
     */
    public Type getType() {
        return TYPE;
    }

    /**
     * Runs this listener's callback.
     * @param state     the state of the event-driven texture
     */
    public void run(V state) {
        requireNonNull(state, "State cannot be null");
        ACTION.accept(state);
    }

}
