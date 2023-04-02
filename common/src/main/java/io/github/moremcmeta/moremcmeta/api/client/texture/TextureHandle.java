/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * References a texture (sprite or individual texture) without providing direct access.
 * @author soir20
 * @since 4.0.0
 */
public final class TextureHandle {
    private final Runnable BIND_FUNCTION;
    private final int MIN_X;
    private final int MIN_Y;
    private final int WIDTH;
    private final int HEIGHT;

    /**
     * Finds all textures whose names match the provided location.
     * @param texturePath       full path of the texture (with .png suffix)
     * @return handles to all textures matching that location
     */
    public static List<TextureHandle> find(ResourceLocation texturePath) {
        requireNonNull(texturePath, "Texture path cannot be null");
        ImmutableList.Builder<TextureHandle> handles = new ImmutableList.Builder<>();

        if (MoreMcmeta.spriteFinder() == null) {
            return handles.build();
        }

        Optional<Sprite> spriteOptional = MoreMcmeta.spriteFinder().findSprite(texturePath);
        if (spriteOptional.isPresent()) {
            Sprite sprite = spriteOptional.get();
            int minX = Point.x(sprite.uploadPoint());
            int minY = Point.y(sprite.uploadPoint());
            handles.add(new TextureHandle(sprite::bind, minX, minY, sprite.width(), sprite.height()));
        }

        AbstractTexture texture = Minecraft.getInstance().getTextureManager()
                .getTexture(texturePath, MissingTextureAtlasSprite.getTexture());
        if (texture != MissingTextureAtlasSprite.getTexture()) {

            /* Currently, there is no way to retrieve the dimensions of a texture w/o Mixins.
               The max integer value is used to reserve the interface w/ dimensions in case
               the real dimensions are retrieved in the future. */
            handles.add(new TextureHandle(texture::bind, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));

        }

        return handles.build();
    }

    /**
     * Creates a new texture handle.
     * @param bindFunction      function to bind this texture in OpenGL
     * @param minX              x-coordinate of the top-left corner of the texture
     * @param minY              y-coordinate of the top-left corner of the texture
     * @param width             width of the texture
     * @param height            height of the texture
     */
    public TextureHandle(Runnable bindFunction, int minX, int minY, int width, int height) {
        BIND_FUNCTION = requireNonNull(bindFunction, "Bind function cannot be null");
        MIN_X = minX;
        MIN_Y = minY;
        if (MIN_X < 0 || MIN_Y < 0) {
            throw new NegativeUploadPointException(MIN_X, MIN_Y);
        }

        WIDTH = width;
        if (WIDTH < 0) {
            throw new NegativeDimensionException(WIDTH);
        }

        HEIGHT = height;
        if (HEIGHT < 0) {
            throw new NegativeDimensionException(HEIGHT);
        }
    }

    /**
     * Binds this texture to OpenGL.
     */
    public void bind() {
        BIND_FUNCTION.run();
    }

    /**
     * Returns the x-coordinate of the top-left corner of the texture. This marks the smallest
     * x-coordinate that should be used to upload an image to this texture.
     * @return x-coordinate of the top-left corner of the texture
     */
    public int minX() {
        return MIN_X;
    }

    /**
     * Returns the y-coordinate of the top-left corner of the texture. This marks the smallest
     * y-coordinate that should be used to upload an image to this texture.
     * @return y-coordinate of the top-left corner of the texture
     */
    public int minY() {
        return MIN_Y;
    }

    /**
     * Returns the width of the texture. <b>This method returns {@link Integer#MAX_VALUE} when
     * the true width is not available.</b> This method is intended to be used for checking whether
     * an upload is in bounds, so the maximum integer value treats everything in bounds when the
     * width is unavailable.
     * @return width of the texture or {@link Integer#MAX_VALUE}
     */
    public int width() {
        return WIDTH;
    }

    /**
     * Returns the height of the texture. <b>This method returns {@link Integer#MAX_VALUE} when
     * the true height is not available.</b> This method is intended to be used for checking whether
     * an upload is in bounds, so the maximum integer value treats everything in bounds when the
     * height is unavailable.
     * @return height of the texture or {@link Integer#MAX_VALUE}
     */
    public int height() {
        return HEIGHT;
    }

}
