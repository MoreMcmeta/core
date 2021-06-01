package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture to an atlas sprite.
 * @author soir20
 */
public class SpriteUploadComponent implements ITextureComponent<NativeImageFrame> {
    private final TextureAtlasSprite SPRITE;

    /**
     * Creates a new component for uploading a texture to an atlas sprite.
     * @param sprite        the sprite to upload the texture to
     */
    public SpriteUploadComponent(TextureAtlasSprite sprite) {
        SPRITE = requireNonNull(sprite, "Sprite cannot be null");
    }

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        Point uploadPoint = getCoordinatesFromSprite(SPRITE);

        TextureListener<NativeImageFrame> uploadListener = new TextureListener<>(TextureListener.Type.UPLOAD,
                (state) -> {
                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() -> state.getImage().uploadAt(uploadPoint));
                    } else {
                        state.getImage().uploadAt(uploadPoint);
                    }
                });

        // We need this listener because atlas sprites will never be bound
        TextureListener<NativeImageFrame> tickListener = new TextureListener<>(TextureListener.Type.TICK,
                (state) -> {
                    SPRITE.atlas().bind();
                    state.getTexture().upload();
                });

        return Stream.of(uploadListener, tickListener);
    }

    /**
     * Gets a sprite's x and y coordinates of its top left corner in its texture atlas.
     * @param sprite    the sprite to get the coordinates of
     * @return the x and y coordinates of the sprite's top left corner
     */
    private Point getCoordinatesFromSprite(TextureAtlasSprite sprite) {
        String spriteStr = sprite.toString();
        int labelLength = 2;

        int xLabelIndex = spriteStr.indexOf("x=");
        int xDelimiterIndex = spriteStr.indexOf(',', xLabelIndex);
        int x = Integer.parseInt(spriteStr.substring(xLabelIndex + labelLength, xDelimiterIndex));

        int yLabelIndex = spriteStr.indexOf("y=");
        int yDelimiterIndex = spriteStr.indexOf(',', yLabelIndex);
        int y = Integer.parseInt(spriteStr.substring(yLabelIndex + labelLength, yDelimiterIndex));

        return new Point(x, y);
    }

}
