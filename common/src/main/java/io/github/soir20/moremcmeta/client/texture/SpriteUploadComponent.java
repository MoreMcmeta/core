package io.github.soir20.moremcmeta.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture to an atlas sprite.
 * @author soir20
 */
public class SpriteUploadComponent implements ITextureComponent {
    private final ISprite SPRITE;

    /**
     * Creates a new component for uploading a texture to an atlas sprite.
     * @param sprite        the sprite to upload the texture to
     */
    public SpriteUploadComponent(ISprite sprite) {
        SPRITE = requireNonNull(sprite, "Sprite cannot be null");
    }

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener> getListeners() {
        Point uploadPoint = SPRITE.getUploadPoint();

        TextureListener uploadListener = new TextureListener(TextureListener.Type.UPLOAD,
                (state) -> {
                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() -> state.getImage().uploadAt(uploadPoint));
                    } else {
                        state.getImage().uploadAt(uploadPoint);
                    }
                });

        // We need this listener because atlas sprites will never be bound
        TextureListener tickListener = new TextureListener(TextureListener.Type.TICK,
                (state) -> {
                    SPRITE.bind();
                    state.getTexture().upload();
                });

        return Stream.of(uploadListener, tickListener);
    }

}
