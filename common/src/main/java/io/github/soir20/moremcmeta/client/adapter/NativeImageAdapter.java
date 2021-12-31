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

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.client.animation.RGBAInterpolator;
import io.github.soir20.moremcmeta.client.texture.RGBAImage;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link NativeImage} so it is compatible with the {@link RGBAImage} interface and
 * the {@link RGBAInterpolator}.
 * @author soir20
 */
public class NativeImageAdapter implements RGBAImage {
    private final NativeImage IMAGE;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int WIDTH;
    private final int HEIGHT;
    private final int MIPMAP_LEVEL;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final boolean AUTO_CLOSE;
    private final VisibleArea VISIBLE_AREA;

    /**
     * Creates a new {@link NativeImage} wrapper for part of an image.
     * @param image         the image to wrap
     * @param xOffset       horizontal offset of the image in a texture
     * @param yOffset       vertical offset of the image in a texture
     * @param width         width of the image
     * @param height        height of the image
     * @param mipmapLevel   mipmap level of the image
     * @param blur          whether to blur this image
     * @param clamp         whether to clamp this image
     * @param autoClose     whether to automatically close this image
     * @param visibleArea   the visible portions of this image
     */
    public NativeImageAdapter(NativeImage image, int xOffset, int yOffset, int width, int height,
                              int mipmapLevel, boolean blur, boolean clamp, boolean autoClose,
                              VisibleArea visibleArea) {
        IMAGE = requireNonNull(image, "Image cannot be null");
        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
        WIDTH = width;
        HEIGHT = height;
        MIPMAP_LEVEL = mipmapLevel;
        BLUR = blur;
        CLAMP = clamp;
        AUTO_CLOSE = autoClose;
        VISIBLE_AREA = requireNonNull(visibleArea, "Visible area cannot be null");
    }

    /**
     * Creates a new {@link NativeImage} wrapper for an entire image. The image
     * is not blurred, clamped, or auto-closed, and it has no visible area.
     * @param image             the image to wrap
     * @param mipmapLevel       mipmap level of the image
     */
    public NativeImageAdapter(NativeImage image, int mipmapLevel) {
        IMAGE = requireNonNull(image, "Image cannot be null");
        X_OFFSET = 0;
        Y_OFFSET = 0;
        WIDTH = image.getWidth();
        HEIGHT = image.getHeight();
        MIPMAP_LEVEL = mipmapLevel;
        BLUR = false;
        CLAMP = false;
        AUTO_CLOSE = false;
        VISIBLE_AREA = (new VisibleArea.Builder()).build();
    }

    /**
     * Gets the color of a pixel in the image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    @Override
    public int getPixel(int x, int y) {
        return IMAGE.getPixelRGBA(x + X_OFFSET, y + Y_OFFSET);
    }

    /**
     * Sets the color of a pixel in the image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    @Override
    public void setPixel(int x, int y, int color) {
        IMAGE.setPixelRGBA(x + X_OFFSET, y + Y_OFFSET, color);
    }

    /**
     * Gets the width of the image.
     * @return  the width of the image in pixels
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Gets the height of the image.
     * @return  the height of the image in pixels
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Gets the visible area (iterable by point) of this image.
     * @return  the visible area of this image
     */
    @Override
    public VisibleArea getVisibleArea() {
        return VISIBLE_AREA;
    }

    /**
     * Uploads the top-left corner of this image at the given coordinates on the render thread.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     */
    @Override
    public void upload(int uploadX, int uploadY) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> uploadImmediately(uploadX, uploadY));
        } else {
            uploadImmediately(uploadX, uploadY);
        }

    }

    /**
     * Gets the original {@link NativeImage} corresponding to this adapter.
     * @return the original {@link NativeImage}
     */
    public NativeImage getImage() {
        return IMAGE;
    }

    /**
     * Uploads this image at the given coordinates immediately.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     */
    private void uploadImmediately(int uploadX, int uploadY) {
        IMAGE.upload(
                MIPMAP_LEVEL, uploadX, uploadY, X_OFFSET, Y_OFFSET,
                WIDTH, HEIGHT, BLUR, CLAMP, MIPMAP_LEVEL > 0, AUTO_CLOSE
        );
    }

}
