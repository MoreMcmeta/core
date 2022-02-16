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

package io.github.soir20.moremcmeta.client.adapter;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.client.animation.RGBAInterpolator;
import io.github.soir20.moremcmeta.client.texture.RGBAImage;

import java.util.ArrayList;
import java.util.List;

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
    private final ClosedStatus CLOSED;

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
     * @param closedStatus  common close status for all adapters sharing the same underlying image.
     *                      If one is closed, then all the images are closed.
     */
    public NativeImageAdapter(NativeImage image, int xOffset, int yOffset, int width, int height,
                              int mipmapLevel, boolean blur, boolean clamp, boolean autoClose,
                              VisibleArea visibleArea, ClosedStatus closedStatus) {
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
        CLOSED = requireNonNull(closedStatus, "Close status cannot be null");
        CLOSED.addSubscriber(this::close);
    }

    /**
     * Creates a new {@link NativeImage} wrapper for an entire image. The image
     * is not blurred, clamped, or auto-closed, and it has no visible area.
     * @param image             the image to wrap
     * @param mipmapLevel       mipmap level of the image
     * @param closedStatus  common close status for all adapters sharing the same underlying image.
     *                      If one is closed, then all the images are closed.
     */
    public NativeImageAdapter(NativeImage image, int mipmapLevel, ClosedStatus closedStatus) {
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
        CLOSED = requireNonNull(closedStatus, "Close status cannot be null");
        CLOSED.addSubscriber(this::close);
    }

    /**
     * Gets the color of a pixel in the image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int getPixel(int x, int y) {
        checkOpen();
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
    public void setPixel(int x, int y, int color) {
        checkOpen();
        IMAGE.setPixelRGBA(x + X_OFFSET, y + Y_OFFSET, color);
    }

    /**
     * Gets the width of the image.
     * @return  the width of the image in pixels
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int getWidth() {
        checkOpen();
        return WIDTH;
    }

    /**
     * Gets the height of the image.
     * @return  the height of the image in pixels
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public int getHeight() {
        checkOpen();
        return HEIGHT;
    }

    /**
     * Gets the visible area (iterable by point) of this image.
     * @return  the visible area of this image
     * @throws IllegalStateException if this image has been closed
     */
    @Override
    public VisibleArea getVisibleArea() {
        checkOpen();
        return VISIBLE_AREA;
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
     * Closes the underlying {@link NativeImage} and any other related resources. Idempotent.
     */
    @Override
    public void close() {

        // NativeImage's close implementation is idempotent.
        IMAGE.close();
        CLOSED.close();

    }

    /**
     * Gets the original {@link NativeImage} corresponding to this adapter.
     * @return the original {@link NativeImage}
     * @throws IllegalStateException if this image has been closed
     */
    public NativeImage getImage() {
        checkOpen();
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

    /**
     * Checks that is image is still open.
     * @throws IllegalStateException if the image is not open
     */
    private void checkOpen() {
        if (CLOSED.isClosed()) {
            throw new IllegalStateException("Image is closed");
        }
    }

    /**
     * Represents a shared open/close status for multiple {@link NativeImageAdapter}s referencing the same or
     * related {@link NativeImage}s. When {@link #close()}, the subscribers run to clean up all the related images
     * at the same time.
     * @author soir20
     */
    public static final class ClosedStatus {
        private final List<Runnable> SUBSCRIBERS;
        private boolean closed;

        /**
         * Creates a new shared status.
         */
        public ClosedStatus() {
            SUBSCRIBERS = new ArrayList<>();
        }

        /**
         * Adds a subscriber that will run when {@link #close()} is called.
         * @param subscriber        code to run to clean up one of the related images
         * @throws IllegalStateException if a subscriber is added after closure has started
         */
        public void addSubscriber(Runnable subscriber) {
            requireNonNull(subscriber, "Subscriber cannot be null");

            if (closed) {
                throw new IllegalStateException("Can't add subscribers after closure begins");
            }

            SUBSCRIBERS.add(subscriber);
        }

        /**
         * Changes the status to a closed state and runs all subscribers. Idempotent.
         */
        public void close() {
            if (closed) {
                return;
            }

            closed = true;
            SUBSCRIBERS.forEach(Runnable::run);
            SUBSCRIBERS.clear();
        }

        /**
         * Gets whether all the related images have started the closure process.
         * @return whether all the subscribers are closing or have closed
         */
        public boolean isClosed() {
            return closed;
        }

    }

}
