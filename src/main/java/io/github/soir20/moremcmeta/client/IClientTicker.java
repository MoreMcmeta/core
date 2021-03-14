package io.github.soir20.moremcmeta.client;

/**
 * Updates a group of objects each client tick. Whether these updates occur
 * at the start or end of the client tick is implementation-defined.
 */
public interface IClientTicker {

    /**
     * Stops updating all of this ticker's textures.
     */
    void stopTicking();

}
