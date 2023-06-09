/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.client.adapter;

import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Atlas;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.ToIntFunction;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link TextureAtlas} to be a {@link Atlas}.
 * @author soir20
 */
public final class AtlasAdapter implements Atlas {
    private final TextureAtlas ATLAS;
    private final ToIntFunction<TextureAtlasSprite> MIPMAP_LEVEL_GETTER;

    /**
     * Creates a new adapter for an atlas at the given location. If no texture exists at the
     * location or the texture there is not an atlas, this adapter will simply act as an
     * empty atlas and provide no sprites.
     * @param location              the location to look for an atlas
     * @param mipmapLevelGetter     gets the mipmap level of this atlas from a sprite
     */
    public AtlasAdapter(ResourceLocation location, ToIntFunction<TextureAtlasSprite> mipmapLevelGetter) {
        requireNonNull(location, "Location cannot be null");
        MIPMAP_LEVEL_GETTER = requireNonNull(mipmapLevelGetter, "Mipmap level getter cannot be null");

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        if (texture instanceof TextureAtlas) {
            ATLAS = (TextureAtlas) texture;
        } else {
            ATLAS = null;
        }
    }

    @Override
    public Optional<Sprite> sprite(ResourceLocation location) {
        requireNonNull(location, "Sprite location cannot be null");

        if (ATLAS == null) {
            return Optional.empty();
        }

        ResourceLocation properSpriteName = SpriteName.fromTexturePath(location);

        TextureAtlasSprite sprite = ATLAS.getSprite(properSpriteName);
        if (sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
            sprite = ATLAS.getSprite(location);
        }

        // Check the original location in case another mod added it by that name
        if (sprite.getName().equals(MissingTextureAtlasSprite.getLocation())) {
            return Optional.empty();
        }

        return Optional.of(new SpriteAdapter(sprite, MIPMAP_LEVEL_GETTER.applyAsInt(sprite)));
    }

    /**
     * Adapts a {@link TextureAtlasSprite} to be a {@link Sprite}.
     * @author soir20
     */
    private static class SpriteAdapter implements Sprite {
        private final TextureAtlasSprite SPRITE;
        private final long UPLOAD_POINT;
        private final int MIPMAP_LEVEL;

        /**
         * Adapts the given sprite to be a {@link Sprite}.
         * @param sprite        the sprite to adapt
         * @param mipmapLevel   the number of mipmaps for this sprite
         */
        public SpriteAdapter(TextureAtlasSprite sprite, int mipmapLevel) {
            SPRITE = sprite;
            UPLOAD_POINT = findUploadPoint();

            if (mipmapLevel < 0) {
                throw new IllegalArgumentException("Sprite cannot have negative mipmaps");
            }

            MIPMAP_LEVEL = mipmapLevel;
        }

        @Override
        public ResourceLocation name() {
            return SPRITE.getName();
        }

        @Override
        public ResourceLocation atlas() {
            return SPRITE.atlas().location();
        }

        @Override
        public long uploadPoint() {
            return UPLOAD_POINT;
        }

        @Override
        public int mipmapLevel() {
            return MIPMAP_LEVEL;
        }

        @Override
        public int width() {
            return SPRITE.getWidth();
        }

        @Override
        public int height() {
            return SPRITE.getHeight();
        }

        /**
         * Gets a sprite's x and y-coordinates of its top left corner in its texture atlas.
         * @return the x and y-coordinates of the sprite's top left corner
         */
        private long findUploadPoint() {
            String spriteStr = SPRITE.toString();
            int labelLength = 2;

            int xLabelIndex = spriteStr.indexOf("x=");
            int xDelimiterIndex = spriteStr.indexOf(',', xLabelIndex);
            int x = Integer.parseInt(spriteStr.substring(xLabelIndex + labelLength, xDelimiterIndex));

            int yLabelIndex = spriteStr.indexOf("y=");
            int yDelimiterIndex = spriteStr.indexOf(',', yLabelIndex);
            int y = Integer.parseInt(spriteStr.substring(yLabelIndex + labelLength, yDelimiterIndex));

            return Point.pack(x, y);
        }

    }
}
