package io.github.soir20.moremcmeta.client;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.BooleanSupplier;

/**
 * Updates items each client tick. Automatically handles Forge event registration.
 * @author soir20
 */
public class ClientTicker {
    private final ImmutableCollection<? extends ITickable> TICKABLES;
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
    public ClientTicker(ImmutableCollection<? extends ITickable> items, IEventBus eventBus,
                        TickEvent.Phase phase, BooleanSupplier condition) {
        TICKABLES = items;
        EVENT_BUS = eventBus;
        PHASE = phase;
        CONDITION = condition;
        isTicking = true;

        EVENT_BUS.register(this);
    }

    /**
     * Updates all the items associated with this ticker.
     * @param event     tick event on the client side
     */
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == PHASE && CONDITION.getAsBoolean()) {
            TICKABLES.forEach(ITickable::tick);
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
