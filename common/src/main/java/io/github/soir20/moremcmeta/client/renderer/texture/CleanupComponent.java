package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Cleans up images when an {@link EventDrivenTexture} closes.
 * @param <I> image type
 * @author soir20
 */
public class CleanupComponent<I> implements ITextureComponent {
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a cleanup component for a texture.
     * @param closeAction       action to clean up all native image resources
     */
    public CleanupComponent(Runnable closeAction) {
        CLOSE_ACTION = requireNonNull(closeAction, "Close action cannot be null");
    }

    /**
     * Gets all the listeners for this component.
     * @return the cleanup listener
     */
    @Override
    public Stream<TextureListener> getListeners() {
        return Stream.of(new TextureListener(TextureListener.Type.CLOSE, (state) -> CLOSE_ACTION.run()));
    }

}
