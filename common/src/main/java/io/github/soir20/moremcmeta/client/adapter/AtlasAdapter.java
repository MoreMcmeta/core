package io.github.soir20.moremcmeta.client.adapter;

import io.github.soir20.moremcmeta.client.texture.IAtlas;
import io.github.soir20.moremcmeta.client.texture.ISprite;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link TextureAtlas} to be a {@link IAtlas}.
 * @author soir20
 */
public class AtlasAdapter implements IAtlas {
    private final TextureAtlas ATLAS;

    /**
     * Creates a new adapter for an atlas at the given location. If no texture exists at the
     * location or the texture there is not an atlas, this adapter will simply act as an
     * empty atlas and provide no sprites.
     * @param location      the location to look for an atlas
     */
    public AtlasAdapter(ResourceLocation location) {
        requireNonNull(location);
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        if (texture instanceof TextureAtlas) {
            ATLAS = (TextureAtlas) texture;
        } else {
            ATLAS = null;
        }
    }

    /**
     * Gets a sprite from its location.
     * @param location      the location of the sprite without its extension
     *                      or the textures directory prefix
     * @return the sprite if found
     */
    @Override
    public Optional<ISprite> getSprite(ResourceLocation location) {
        if (ATLAS == null) {
            return Optional.empty();
        }

        TextureAtlasSprite sprite = ATLAS.getSprite(location);
        if (sprite == null) {
            return Optional.empty();
        }

        return Optional.of(new SpriteAdapter(sprite));
    }

    /**
     * Adapts a {@link TextureAtlasSprite} to be a {@link ISprite}.
     * @author soir20
     */
    private static class SpriteAdapter implements ISprite {
        private final TextureAtlasSprite SPRITE;
        private final Point UPLOAD_POINT;

        /**
         * Adapts the given sprite to be a {@link ISprite}.
         * @param sprite    the sprite to adapt
         */
        public SpriteAdapter(TextureAtlasSprite sprite) {
            SPRITE = sprite;
            UPLOAD_POINT = findUploadPoint();
        }

        /**
         * Binds this sprite (actually its atlas) to OpenGL.
         */
        @Override
        public void bind() {
            SPRITE.atlas().bind();
        }

        /**
         * Gets the name of this sprite.
         * @return the sprite's name
         */
        @Override
        public ResourceLocation getName() {
            return SPRITE.getName();
        }

        /**
         * Gets the coordinates of the top-left corner of this sprite
         * on its atlas, which is where it should be uploaded to.
         * @return the sprite's upload point
         */
        @Override
        public Point getUploadPoint() {
            return UPLOAD_POINT;
        }

        /**
         * Gets a sprite's x and y coordinates of its top left corner in its texture atlas.
         * @return the x and y coordinates of the sprite's top left corner
         */
        private Point findUploadPoint() {
            String spriteStr = SPRITE.toString();
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
}
