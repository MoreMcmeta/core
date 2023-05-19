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

import io.github.moremcmeta.moremcmeta.api.math.NegativeDimensionException;

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
