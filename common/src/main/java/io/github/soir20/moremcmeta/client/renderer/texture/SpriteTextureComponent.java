package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.stream.Stream;

/**
 * A component with registration and upload listeners for textures that are
 * part of an atlas.
 */
public class SpriteTextureComponent implements ITextureComponent<NativeImageFrame> {
    private final Point UPLOAD_POINT;

    /**
     * Creates a new sprite upload component.
     * @param sprite    the sprite to upload to
     */
    public SpriteTextureComponent(TextureAtlasSprite sprite) {
        UPLOAD_POINT = getCoordinatesFromSprite(sprite);
    }

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        TextureListener<NativeImageFrame> registrationListener =
                new TextureListener<>(TextureListener.Type.REGISTRATION, (state) -> {
                    NativeImageFrame image = state.getImage();
                    TextureUtil.prepareImage(state.getTextureId(), image.getMipmapLevel(),
                            image.getWidth(), image.getHeight());
                });

        TextureListener<NativeImageFrame> uploadListener = new TextureListener<>(TextureListener.Type.UPLOAD,
                (state) -> state.getImage().uploadAt(UPLOAD_POINT));

        return Stream.of(registrationListener, uploadListener);
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