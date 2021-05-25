package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import static java.util.Objects.requireNonNull;

/**
 * An animated texture that is part of an atlas.
 * @param <F>
 * @author soir20
 */
public class AnimatedSprite<F extends IAnimationFrame> extends AnimatedTexture<F> {
    private final Runnable CLOSE_ACTION;
    private final TextureAtlasSprite SPRITE;

    private Pair<Integer, Integer> topLeftCoordinates;

    /**
     * Creates a new animated texture that is not part of an atlas.
     * @param data          texture data
     * @param closeAction   cleans up the texture when it is closed
     * @param sprite        atlas sprite associated with this texture
     */
    public AnimatedSprite(AnimatedTextureData<F> data, Runnable closeAction, TextureAtlasSprite sprite) {
        super(data);
        CLOSE_ACTION = closeAction;
        SPRITE = requireNonNull(sprite, "Sprite must not be null");
    }

    /**
     * Binds this texture to OpenGL for rendering. Interpolation (if used) occurs at this point as well.
     */
    @Override
    public void bind() {

        /* If the animated texture is part of an atlas, this texture will
           only be bound if we are animating it (and need to change the atlas) */
        SPRITE.atlas().bind();
        uploadCurrentFrame();

    }

    /**
     * Loads this image immediately to OpenGL. Does nothing because we will upload
     * to an already-created atlas.
     */
    @Override
    protected void loadImage() {}

    /**
     * Uploads the current frame to OpenGL immediately.
     */
    private void uploadCurrentFrame() {
        if (topLeftCoordinates == null) {
            topLeftCoordinates = getCoordinatesFromSprite(SPRITE);
        }

        getData().getFrameManager().getCurrentFrame().uploadAt(topLeftCoordinates.getFirst(),
                topLeftCoordinates.getSecond());
    }

    /**
     * Gets a sprite's x and y coordinates of its top left corner in its texture atlas.
     * @param sprite    the sprite to get the coordinates of
     * @return the x and y coordinates of the sprite's top left corner
     */
    private Pair<Integer, Integer> getCoordinatesFromSprite(TextureAtlasSprite sprite) {
        String spriteStr = sprite.toString();
        int labelLength = 2;

        int xLabelIndex = spriteStr.indexOf("x=");
        int xDelimiterIndex = spriteStr.indexOf(',', xLabelIndex);
        int x = Integer.parseInt(spriteStr.substring(xLabelIndex + labelLength, xDelimiterIndex));

        int yLabelIndex = spriteStr.indexOf("y=");
        int yDelimiterIndex = spriteStr.indexOf(',', yLabelIndex);
        int y = Integer.parseInt(spriteStr.substring(yLabelIndex + labelLength, yDelimiterIndex));

        return new Pair<>(x, y);
    }

    /**
     * Closes all resources this texture uses. Does not close atlas textures or sprites.
     */
    @Override
    public void close() {
        CLOSE_ACTION.run();
    }

}