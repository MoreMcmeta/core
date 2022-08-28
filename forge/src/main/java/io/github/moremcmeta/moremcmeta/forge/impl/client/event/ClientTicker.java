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

package io.github.moremcmeta.moremcmeta.forge.impl.client.event;

import com.google.common.collect.ImmutableCollection;
import io.github.moremcmeta.moremcmeta.impl.client.texture.CustomTickable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BooleanSupplier;

import static java.util.Objects.requireNonNull;

/**
 * Updates items each client tick. Automatically handles Forge event registration.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class ClientTicker {
    private final ImmutableCollection<? extends CustomTickable> TICKABLES;
    private final IEventBus EVENT_BUS;
    private final TickEvent.Phase PHASE;
    private final BooleanSupplier CONDITION;
    private boolean isTicking;

    /**
     * Creates and starts a ticker that will update the given items.
     * @param items      the items to update on each tick
     * @param eventBus   the event bus to register this ticker to (use the general bus)
     * @param phase      the phase to update
     * @param condition  any additional conditions for updating the items
     */
    public ClientTicker(ImmutableCollection<? extends CustomTickable> items, IEventBus eventBus,
                        TickEvent.Phase phase, BooleanSupplier condition) {

        TICKABLES = requireNonNull(items, "Tickable items container cannot be null");
        EVENT_BUS = requireNonNull(eventBus, "Event bus cannot be null");
        PHASE = requireNonNull(phase, "Tick phase cannot be null");
        CONDITION = requireNonNull(condition, "Tick condition cannot be null");
        isTicking = true;

        EVENT_BUS.register(this);
    }

    /**
     * Updates all the items associated with this ticker.
     * @param event     tick event on the client side
     */
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        requireNonNull(event, "Tick event cannot be null");

        if (event.phase == PHASE && CONDITION.getAsBoolean()) {
            TICKABLES.forEach(CustomTickable::tick);
        }
    }

    /**
     * Stops ticking the current items. Does nothing if ticking has already stopped.
     */
    public void stopTicking() {
        if (isTicking) {
            EVENT_BUS.unregister(this);
            isTicking = false;
        }
    }

}
