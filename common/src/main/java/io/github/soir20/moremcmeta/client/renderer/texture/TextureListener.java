package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.function.Consumer;

/**
 * A listener for an {@link EventDrivenTexture}'s events.
 * @param <I> image type
 */
public class TextureListener<I> {

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
    private final Consumer<EventDrivenTexture.TextureState<I>> ACTION;

    /**
     * Creates a listener for a certain type of event.
     * @param type      event type
     * @param action    callback to execute when the event occurs
     */
    public TextureListener(Type type, Consumer<EventDrivenTexture.TextureState<I>> action) {
        TYPE = type;
        ACTION = action;
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
    public void run(EventDrivenTexture.TextureState<I> state) {
        ACTION.accept(state);
    }

}
