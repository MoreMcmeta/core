package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.stream.Stream;

/**
 * Cleans up a set of {@link NativeImage}s when an {@link EventDrivenTexture} closes.
 */
public class NativeImageCleanupComponent implements ITextureComponent<NativeImageFrame> {
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a cleanup component for a texture.
     * @param closeAction       action to clean up all native image resources
     */
    public NativeImageCleanupComponent(Runnable closeAction) {
        CLOSE_ACTION = closeAction;
    }

    /**
     * Gets all the listeners for this component.
     * @return the cleanup listener
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        return Stream.of(new TextureListener<>(TextureListener.Type.CLOSE, (state) -> CLOSE_ACTION.run()));
    }

}
