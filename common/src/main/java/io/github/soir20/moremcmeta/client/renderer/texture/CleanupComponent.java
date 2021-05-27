package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.stream.Stream;

/**
 * Cleans up images when an {@link EventDrivenTexture} closes.
 * @param <I> image type
 */
public class CleanupComponent<I> implements ITextureComponent<I> {
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a cleanup component for a texture.
     * @param closeAction       action to clean up all native image resources
     */
    public CleanupComponent(Runnable closeAction) {
        CLOSE_ACTION = closeAction;
    }

    /**
     * Gets all the listeners for this component.
     * @return the cleanup listener
     */
    @Override
    public Stream<TextureListener<I>> getListeners() {
        return Stream.of(new TextureListener<>(TextureListener.Type.CLOSE, (state) -> CLOSE_ACTION.run()));
    }

}
