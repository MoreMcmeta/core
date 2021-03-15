package io.github.soir20.moremcmeta.client;

import net.minecraftforge.event.TickEvent;

/**
 * Updates items each client tick. Whether these updates occur at the start or
 * end of each tick is implementation-defined.
 */
public interface IClientTicker {

    /**
     * Updates this ticker, updating all of its items.
     * @param event     the client tick event
     */
    void tick(TickEvent.ClientTickEvent event);

    /**
     * Stops this ticker from updating. Make a new one to restart ticking.
     */
    void stopTicking();

}
