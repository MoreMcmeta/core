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

package io.github.soir20.moremcmeta.impl.client.adapter;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link NativeImage} so it is compatible with the {@link CloseableImage} interface.
 * @author soir20
 */
public class NativeImageAdapter implements CloseableImage {
    private final NativeImage IMAGE;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int WIDTH;
    private final int HEIGHT;
    private final int MIPMAP_LEVEL;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final boolean AUTO_CLOSE;
    private final AtomicBoolean CLOSED;

    /**
     * Creates a new {@link NativeImage} wrapper for an entire image. The image
     * is not blurred, clamped, or auto-closed, and it has no visible area.
     * @param image             the image to wrap
     * @param mipmapLevel       mipmap level of the image
     * @param blur              whether to blur the image
     * @param clamp             whether to clamp the image
     */
    public NativeImageAdapter(NativeImage image, int mipmapLevel, boolean blur, boolean clamp) {
        this(image, 0, 0, image.getWidth(), image.getHeight(), mipmapLevel, blur, clamp, false, new AtomicBoolean());
    }

    /**
     * Gets the color of a pixel in the image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int color(int x, int y) {
        checkOpen();
        checkInBounds(x, y);
        return IMAGE.getPixelRGBA(x + X_OFFSET, y + Y_OFFSET);
    }

    /**
     * Sets the color of a pixel in the image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public void setColor(int x, int y, int color) {
        checkOpen();
        checkInBounds(x, y);
        IMAGE.setPixelRGBA(x + X_OFFSET, y + Y_OFFSET, color);
    }

    /**
     * Gets the width of the image.
     * @return  the width of the image in pixels
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int width() {
        checkOpen();
        return WIDTH;
    }

    /**
     * Gets the height of the image.
     * @return  the height of the image in pixels
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int height() {
        checkOpen();
        return HEIGHT;
    }

    /**
     * Uploads the top-left corner of this image at the given coordinates on the render thread.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public void upload(int uploadX, int uploadY) {
        checkOpen();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> uploadImmediately(uploadX, uploadY));
        } else {
            uploadImmediately(uploadX, uploadY);
        }

    }

    /**
     * Takes a portion of this image as a separate {@link CloseableImage}. This image will be
     * closed when the sub-image is closed and vice versa. Changes in the original image or any of
     * its sub-images will be reflected in all the sub-images.
     * @param topLeftX      x-coordinate of the top-left corner of the sub-image
     * @param topLeftY      y-coordinate of the top-left corner of the sub-image
     * @param width         width of the sub-image
     * @param height        height of the sub-image
     * @return the corresponding sub-image
     */
    @Override
    public CloseableImage subImage(int topLeftX, int topLeftY, int width, int height) {
        return new NativeImageAdapter(
                IMAGE,
                topLeftX, topLeftY,
                width, height,
                MIPMAP_LEVEL,
                BLUR, CLAMP,
                AUTO_CLOSE,
                CLOSED
        );
    }

    /**
     * Closes the underlying {@link NativeImage} and any other related resources. Idempotent.
     */
    @Override
    public void close() {

        // NativeImage's close implementation is idempotent.
        CLOSED.set(true);
        IMAGE.close();

    }

    /**
     * Gets whether this image is blurred.
     * @return whether this image is blurred
     */
    public boolean blur() {
        return BLUR;
    }

    /**
     * Gets whether this image is clamped.
     * @return whether this image is clamped
     */
    public boolean clamp() {
        return CLAMP;
    }

    /**
     * Gets the original {@link NativeImage} corresponding to this adapter.
     * @return the original {@link NativeImage}
     * @throws IllegalStateException if this image has been closed
     */
    public NativeImage image() {
        checkOpen();
        return IMAGE;
    }

    /**
     * Creates a new {@link NativeImage} wrapper for part of an image.
     * @param image                 the image to wrap
     * @param xOffset               horizontal offset of the image in a texture
     * @param yOffset               vertical offset of the image in a texture
     * @param width                 width of the image
     * @param height                height of the image
     * @param mipmapLevel           mipmap level of the image
     * @param blur                  whether to blur this image
     * @param clamp                 whether to clamp this image
     * @param autoClose             whether to automatically close this image
     * @param sharedCloseStatus     shared status between all images connected to the same
     *                              {@link NativeImage}
     */
    private NativeImageAdapter(NativeImage image, int xOffset, int yOffset, int width, int height,
                               int mipmapLevel, boolean blur, boolean clamp, boolean autoClose,
                               AtomicBoolean sharedCloseStatus) {
        IMAGE = requireNonNull(image, "Image cannot be null");

        if (xOffset < 0) {
            throw new IllegalArgumentException("X offset cannot be negative");
        }

        if (yOffset < 0) {
            throw new IllegalArgumentException("Y offset cannot be negative");
        }

        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be negative");
        }

        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative");
        }

        if (mipmapLevel < 0) {
            throw new IllegalArgumentException("Mipmap level cannot be negative");
        }

        if (xOffset + width < 0 || xOffset + width > image.getWidth()
                || yOffset + height < 0 || yOffset + height > image.getHeight()) {
            throw new IllegalArgumentException("Sub image extends beyond original image");
        }

        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
        WIDTH = width;
        HEIGHT = height;
        MIPMAP_LEVEL = mipmapLevel;
        BLUR = blur;
        CLAMP = clamp;
        AUTO_CLOSE = autoClose;
        CLOSED = requireNonNull(sharedCloseStatus, "Close status cannot be null");
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

    /**
     * Checks that is image is still open.
     * @throws IllegalStateException if the image is not open
     */
    private void checkOpen() {
        if (CLOSED.get()) {
            throw new IllegalStateException("Image is closed");
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if the point is outside the image bounds.
     * @param x     x-coordinate to check
     * @param y     y-coordinate to check
     */
    private void checkInBounds(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) {
            throw new IllegalArgumentException(String.format(
                    "Tried to access point outside %sx%s image: (%s, %s)", WIDTH, HEIGHT, x, y
            ));
        }
    }

}
