package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Determines if a texture is a sprite and uploads it to the appropriate location.
 */
public class UploadComponent implements ITextureComponent<NativeImageFrame> {
    private final ResourceLocation TEXTURE_LOCATION;
    private final SpriteFinder SPRITE_FINDER;
    private boolean hasBeenBound;
    private Point uploadPoint;

    @Nullable
    private TextureAtlasSprite atlasSprite;

    /**
     * Creates a new upload component.
     * @param textureLocation       location of texture being uploaded
     */
    public UploadComponent(ResourceLocation textureLocation) {
        TEXTURE_LOCATION = textureLocation;
        SPRITE_FINDER = new SpriteFinder();
        uploadPoint = new Point(0, 0);
    }

    /**
     * Gets the listeners for this component.
     * @return all of the listeners for this component
     */
    @Override
    public Stream<TextureListener<NativeImageFrame>> getListeners() {
        TextureListener<NativeImageFrame> firstBindListener =
                new TextureListener<>(TextureListener.Type.BIND, (state) -> {
                    if (hasBeenBound) {
                        return;
                    }
                    hasBeenBound = true;

                    Optional<TextureAtlasSprite> sprite = SPRITE_FINDER.findSprite(TEXTURE_LOCATION);
                    if (sprite.isPresent()) {
                        atlasSprite = sprite.get();
                        uploadPoint = getCoordinatesFromSprite(atlasSprite);

                        // We do not need to prepare an image for sprite textures
                        return;

                    }

                    NativeImageFrame image = state.getImage();

                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() ->
                                TextureUtil.prepareImage(state.getTexture().getId(), image.getMipmapLevel(),
                                        image.getWidth(), image.getHeight()));
                    } else {
                        TextureUtil.prepareImage(state.getTexture().getId(), image.getMipmapLevel(),
                                image.getWidth(), image.getHeight());
                    }

                });

        TextureListener<NativeImageFrame> uploadListener =
                new TextureListener<>(TextureListener.Type.UPLOAD, (state) -> {
                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() -> state.getImage().uploadAt(uploadPoint));
                    } else {
                        state.getImage().uploadAt(uploadPoint);
                    }
             });

        // We need this listener because atlas sprites will never be bound
        TextureListener<NativeImageFrame> tickListener =
                new TextureListener<>(TextureListener.Type.TICK, (state) -> {
                    if (!hasBeenBound) {
                        firstBindListener.run(state);
                    }

                    if (atlasSprite != null && state.needsUpload()) {
                        atlasSprite.atlas().bind();
                        state.getTexture().upload();
                    }
                });

        return Stream.of(firstBindListener, uploadListener, tickListener);
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
