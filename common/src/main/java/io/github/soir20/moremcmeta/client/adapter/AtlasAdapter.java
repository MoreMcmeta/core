/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.adapter;

import io.github.soir20.moremcmeta.client.texture.Atlas;
import io.github.soir20.moremcmeta.client.texture.Sprite;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Adapts a {@link TextureAtlas} to be a {@link Atlas}.
 * @author soir20
 */
public class AtlasAdapter implements Atlas {
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
    public Optional<Sprite> getSprite(ResourceLocation location) {
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
     * Adapts a {@link TextureAtlasSprite} to be a {@link Sprite}.
     * @author soir20
     */
    private static class SpriteAdapter implements Sprite {
        private final TextureAtlasSprite SPRITE;
        private final Point UPLOAD_POINT;

        /**
         * Adapts the given sprite to be a {@link Sprite}.
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
