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

package io.github.soir20.moremcmeta.impl.client.texture;

import io.github.soir20.moremcmeta.api.math.Point;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mocks an {@link CloseableImage}. Keeps track of set pixel colors.
 * @author soir20
 */
public class MockCloseableImage implements CloseableImage {
    public static final int DEFAULT_DIMENSION = 100;

    private final int[][] PIXELS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private Point uploadPoint;
    private final AtomicBoolean CLOSED;

    public MockCloseableImage() {
        this(DEFAULT_DIMENSION, DEFAULT_DIMENSION);
    }

    public MockCloseableImage(int width, int height) {
        PIXELS = new int[width][height];
        WIDTH = width;
        HEIGHT = height;
        X_OFFSET = 0;
        Y_OFFSET = 0;
        CLOSED = new AtomicBoolean();
    }

    public MockCloseableImage(int[][] pixels) {
        this(pixels, 0, 0, pixels.length, pixels[0].length, new AtomicBoolean());
    }

    private MockCloseableImage(int[][] pixels, int xOffset, int yOffset, int width, int height,
                               AtomicBoolean closedStatus) {
        int maxX = xOffset + width;
        int maxY = yOffset + height;
        if (maxX < 0 || maxX > pixels.length || maxY < 0 || maxY > pixels[0].length) {
            throw new MockSubImageOutsideOriginalException();
        }

        PIXELS = pixels;
        WIDTH = width;
        HEIGHT = height;
        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
        CLOSED = closedStatus;
    }

    @Override
    public int color(int x, int y) {
        if (CLOSED.get()) {
            throw new IllegalStateException("Mock image closed");
        }

        x += X_OFFSET;
        y += Y_OFFSET;

        if (x >= width() || x < 0 || y >= height() || y < 0) {
            throw new MockPixelOutOfBoundsException(x, y);
        }

        return PIXELS[x][y];
    }

    @Override
    public void setColor(int x, int y, int color) {
        if (CLOSED.get()) {
            throw new IllegalStateException("Mock image closed");
        }

        x += X_OFFSET;
        y += Y_OFFSET;

        if (x >= width() || x < 0 || y >= height() || y < 0) {
            throw new MockPixelOutOfBoundsException(x, y);
        }

        PIXELS[x][y] = color;
    }

    @Override
    public int width() {
        if (CLOSED.get()) {
            throw new IllegalStateException("Mock image closed");
        }

        return WIDTH;
    }

    @Override
    public int height() {
        if (CLOSED.get()) {
            throw new IllegalStateException("Mock image closed");
        }

        return HEIGHT;
    }

    @Override
    public void upload(int uploadX, int uploadY) {
        if (CLOSED.get()) {
            throw new IllegalStateException("Mock image closed");
        }

        uploadPoint = new Point(uploadX, uploadY);
    }

    @Override
    public CloseableImage subImage(int topLeftX, int topLeftY, int width, int height) {
        return new MockCloseableImage(PIXELS, topLeftX, topLeftY, width, height, CLOSED);
    }

    @Override
    public void close() {
        CLOSED.set(true);
    }

    public boolean isClosed() {
        return CLOSED.get();
    }

    public Point lastUploadPoint() {
        return uploadPoint;
    }

    public boolean hasSamePixels(MockCloseableImage other) {
        if (width() != other.width() || height() != other.height()) {
            return false;
        }

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (color(x, y) != other.color(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Mock exception thrown when a pixel out of bounds is accessed.
     * @author soir20
     */
    public static class MockPixelOutOfBoundsException extends RuntimeException {

        public MockPixelOutOfBoundsException(int x, int y) {
            super("Point out of bounds: (" + x + ", " + y + ")");
        }

    }

    /**
     * Mock exception thrown when a sub-image that extends beyond the original is created.
     * @author soir20
     */
    public static class MockSubImageOutsideOriginalException extends RuntimeException {

        public MockSubImageOutsideOriginalException() {
            super("Sub-image extends beyond original");
        }

    }

}
