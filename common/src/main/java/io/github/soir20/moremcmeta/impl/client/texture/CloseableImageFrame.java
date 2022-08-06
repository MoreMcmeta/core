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

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.api.client.texture.Color;
import io.github.soir20.moremcmeta.api.client.texture.ColorTransform;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import io.github.soir20.moremcmeta.api.math.Area;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.adt.SparseIntMatrix;
import io.github.soir20.moremcmeta.impl.client.io.FrameReader;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link CloseableImage}.
 * @author soir20
 */
public class CloseableImageFrame {
    private final int WIDTH;
    private final int HEIGHT;
    private final ImmutableList<Layer> LOWER_LAYERS;
    private final TopLayer TOP_LAYER;
    private final int TOP_LAYER_INDEX;
    private ImmutableList<? extends CloseableImage> mipmaps;
    private boolean closed;

    /**
     * Creates a new frame based on frame data.
     * @param frameData             general data associated with the frame
     * @param mipmaps               mipmapped images for this frame (starting with the original image)
     * @param layers                number of layers in the frame
     */
    public CloseableImageFrame(FrameReader.FrameData frameData, ImmutableList<? extends CloseableImage> mipmaps,
                               int layers) {
        requireNonNull(frameData, "Frame data cannot be null");
        this.mipmaps = requireNonNull(mipmaps, "Mipmaps cannot be null");
        if (mipmaps.isEmpty()) {
            throw new IllegalArgumentException("At least one mipmap must be provided");
        }

        // Check mipmap sizes
        int frameWidth = frameData.width();
        int frameHeight = frameData.height();

        for (int level = 0; level < mipmaps.size(); level++) {
            CloseableImage image = mipmaps.get(level);
            int imageWidth = image.width();
            int imageHeight = image.height();

            if (imageWidth != frameWidth >> level || imageHeight != frameHeight >> level) {
                throw new IllegalArgumentException(String.format(
                        "Mipmap %s of size %sx%s conflicts with frame size %sx%s",
                        level, imageWidth, imageHeight, frameWidth, frameHeight
                ));
            }
        }

        WIDTH = frameWidth;
        HEIGHT = frameHeight;
        closed = false;

        // Create layers
        if (layers < 1) {
            throw new IllegalArgumentException(String.format("Layers must be positive: %s", layers));
        }

        // We can represent at most 128 non-negative indices with the byte type
        if (layers > 128) {
            throw new IllegalArgumentException(String.format("Maximum number of layers is 128, was %s", layers));
        }

        TOP_LAYER = new TopLayer(mipmaps.get(0), WIDTH, HEIGHT, (byte) (layers - 1));
        ImmutableList.Builder<Layer> layerBuilder = new ImmutableList.Builder<>();

        if (layers > 1) {
            BottomLayer bottomLayer = new BottomLayer(TOP_LAYER, WIDTH, HEIGHT);
            TOP_LAYER.setBottomLayer(bottomLayer);

            Layer lastLayer = bottomLayer;
            layerBuilder.add(lastLayer);

            for (byte layerIndex = 1; layerIndex < layers - 1; layerIndex++) {
                lastLayer = new MiddleLayer(TOP_LAYER, lastLayer, WIDTH, HEIGHT, layerIndex);
                layerBuilder.add(lastLayer);
            }
        }

        LOWER_LAYERS = layerBuilder.build();
        TOP_LAYER_INDEX = LOWER_LAYERS.size();
    }

    /**
     * Gets the color of the given pixel in the top-level mipmap of this frame.
     * @param x     x-coordinate of the pixel (from the top left)
     * @param y     y-coordinate of the pixel (from the top left)
     * @return the color of the pixel at the given coordinate
     * @throws IllegalStateException if this frame has been closed
     */
    public int color(int x, int y) {
        checkOpen();
        return mipmaps.get(0).color(x, y);
    }

    /**
     * Uploads this frame at a given position in the active texture.
     * @param point     point to upload the top-left corner of this frame at
     * @throws IllegalStateException if this frame has been closed
     */
    public void uploadAt(Point point) {
        checkOpen();

        requireNonNull(point, "Point cannot be null");

        if (point.x() < 0 || point.y() < 0) {
            throw new IllegalArgumentException("Point coordinates must be greater than zero");
        }

        for (int level = 0; level < mipmaps.size(); level++) {
            int mipmappedX = point.x() >> level;
            int mipmappedY = point.y() >> level;

            CloseableImage mipmap = mipmaps.get(level);
            int mipmappedWidth = mipmap.width();
            int mipmappedHeight = mipmap.height();

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                mipmap.upload(mipmappedX, mipmappedY);
            }
        }
    }

    /**
     * Gets the width of this frame in pixels.
     * @return  the width of this frame in pixels
     * @throws IllegalStateException if this frame has been closed
     */
    public int width() {
        checkOpen();
        return WIDTH;
    }

    /**
     * Gets the height of this frame in pixels.
     * @return  the height of this frame in pixels
     * @throws IllegalStateException if this frame has been closed
     */
    public int height() {
        checkOpen();
        return HEIGHT;
    }

    /**
     * Gets the mipmap level of this frame.
     * @return the mipmap level of this frame
     * @throws IllegalStateException if this frame has been closed
     */
    public int mipmapLevel() {
        checkOpen();
        return mipmaps.size() - 1;
    }

    /**
     * Lowers the mipmap level of this frame, closing all mipmaps above the provided level.
     * @param newMipmapLevel        the maximum new mipmap level
     * @throws IllegalStateException if this frame has been closed
     */
    public void lowerMipmapLevel(int newMipmapLevel) {
        checkOpen();

        if (newMipmapLevel == mipmapLevel()) {
            return;
        }

        if (newMipmapLevel < 0) {
            throw new IllegalArgumentException("New mipmap level must be at least zero");
        }

        if (newMipmapLevel > mipmapLevel()) {
            throw new IllegalArgumentException("New mipmap level " + newMipmapLevel + " is greater than current " +
                    "mipmap level " + mipmapLevel());
        }

        for (int level = newMipmapLevel + 1; level < mipmaps.size(); level++) {
            mipmaps.get(level).close();
        }

        mipmaps = mipmaps.subList(0, newMipmapLevel + 1);
    }

    /**
     * Copies pixels from another frame onto this frame. The other frame cannot
     * have a lower mipmap level than this frame. The size of the copied area
     * will be the smaller of the two frame widths and the smaller of the two
     * frame heights.
     * @param source     the source frame to copy from
     * @throws IllegalStateException if this frame has been closed
     */
    public void copyFrom(CloseableImageFrame source) {
        checkOpen();

        requireNonNull(source, "Source cannot be null");

        if (source.mipmapLevel() < mipmapLevel()) {
            throw new IllegalArgumentException("Other frame cannot have lower mipmap level");
        }

        for (int level = 0; level <= mipmapLevel(); level++) {
            mipmaps.get(level).copyFrom(source.mipmaps.get(level));
        }
    }

    /**
     * Applies the given transformation to this frame. The transform is applied
     * directly to the topmost mipmap only. An equivalent transformation for all
     * mipmaps will be computed. A {@link IllegalArgumentException} will be thrown
     * upon attempting to apply the transformation to a point outside the frame
     * bounds.
     * @param transform     transform to apply
     * @param applyArea     area to apply the transformation to
     * @param dependencies  points whose colors the given transform is dependent on
     * @param layer         the index of the layer to apply the transformation to
     * @throws IllegalStateException if this frame has been closed
     * @throws ColorTransform.NonDependencyRequestException if the previous color of a point that
     *                                                     is not a dependency is requested
     */
    public void applyTransform(ColorTransform transform, Area applyArea, Area dependencies, int layer) {
        checkOpen();

        requireNonNull(transform, "Transform cannot be null");
        requireNonNull(applyArea, "Apply area cannot be null");
        requireNonNull(dependencies, "Dependencies cannot be null");

        if (layer < 0) {
            throw new IllegalArgumentException(String.format("Layer index cannot be negative: %s", layer));
        }

        // We omit the top layer from the list of lower layers, so do not check for equality
        if (layer > TOP_LAYER_INDEX) {
            throw new IllegalArgumentException(
                    String.format("Layer index is out of bounds: %s when the max is %s", layer, TOP_LAYER_INDEX)
            );
        }

        Layer layerBelow = layerBelow(layer);
        Map<Point, Color> previousColors = new HashMap<>();
        dependencies.forEach((point) -> {
            requireNonNull(point, "Dependency point cannot be null");
            checkPointInBounds(point);

            previousColors.put(point, layerBelow.read(point));
        });

        // Apply transformation to the original image
        Layer thisLayer = layer == TOP_LAYER_INDEX ? TOP_LAYER : LOWER_LAYERS.get(layer);
        applyArea.forEach((point) -> {
            requireNonNull(point, "Apply area point cannot be null");
            checkPointInBounds(point);

            Color newColor = transform.transform(
                    point,
                    (requestedPoint) -> colorIfDependency(requestedPoint, previousColors)
            );

            thisLayer.write(point, newColor);
        });

        /* Update corresponding mipmap pixels.
           Plugins have no knowledge of mipmaps, and giving them that
           knowledge would require all of them to handle additional
           complexity. Instead, we can efficiently calculate the mipmaps
           ourselves. */
        while (!TOP_LAYER.lastModified().isEmpty()) {
            Point point = TOP_LAYER.lastModified().remove();

            int x = point.x();
            int y = point.y();
            for (int level = 1; level <= mipmapLevel(); level++) {

                // Don't try to set a color when the mipmap is empty
                if (mipmaps.get(level).width() == 0 && mipmaps.get(level).height() == 0) {
                    break;
                }

                int cornerX = makeEven(x >> (level - 1));
                int cornerY = makeEven(y >> (level - 1));

                CloseableImage prevImage = mipmaps.get(level - 1);
                int topLeft = prevImage.color(cornerX, cornerY);
                int topRight = prevImage.color(cornerX + 1, cornerY);
                int bottomLeft = prevImage.color(cornerX, cornerY + 1);
                int bottomRight = prevImage.color(cornerX + 1, cornerY + 1);

                int blended = ColorBlender.blend(
                        topLeft,
                        topRight,
                        bottomLeft,
                        bottomRight
                );

                mipmaps.get(level).setColor(x >> level, y >> level, blended);
            }
        }

    }

    /**
     * Gets the number of layers in this frame.
     * @return the number of layers in this frame
     */
    public int layers() {
        return TOP_LAYER_INDEX + 1;
    }

    /**
     * Closes all resources associated with this frame. Idempotent.
     */
    public void close() {
        closed = true;
        mipmaps.forEach(CloseableImage::close);
    }

    /**
     * Checks if this frame is closed and throws an exception if so; otherwise, does nothing.
     * @throws IllegalStateException if this frame has been closed
     */
    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Frame is closed");
        }
    }

    /**
     * Gets the layer below the layer with the given index. If there are no layers below,
     * returns the bottommost layer.
     * @param layer     index of the layer above the layer to retrieve
     * @return the layer below the layer with the given index or the bottommost layer
     */
    private Layer layerBelow(int layer) {
        if (TOP_LAYER_INDEX == 0) {
            return TOP_LAYER;
        }

        if (layer == 0) {
            return LOWER_LAYERS.get(0);
        }

        return LOWER_LAYERS.get(layer - 1);
    }

    /**
     * Checks if a point is inside this layer's boundaries.
     * @param point     the point to check
     * @throws FrameView.PixelOutOfBoundsException if the point is out of bounds
     */
    private void checkPointInBounds(Point point) throws FrameView.PixelOutOfBoundsException {
        if (point.x() < 0 || point.y() < 0 || point.x() >= WIDTH || point.y() >= HEIGHT) {
            throw new FrameView.PixelOutOfBoundsException(point.x(), point.y());
        }
    }

    /**
     * Retrieves the current color of a point that a transform depends on, or throws an
     * exception if the transform did not request the point as a dependency.
     * @param requestedPoint        point the transform requested
     * @param dependencies          all the transform's dependencies and their current colors
     * @return the color of the requested point
     * @throws ColorTransform.NonDependencyRequestException if the point is not a dependency
     *                                                     of the transform
     */
    private Color colorIfDependency(Point requestedPoint, Map<Point, Color> dependencies) {
        Color color = dependencies.get(requestedPoint);

        if (color == null) {
            throw new ColorTransform.NonDependencyRequestException();
        }

        return color;
    }

    /**
     * Convert a number to the closest even integer that is smaller than
     * the given integer.
     * @param num           number to make even
     * @return the closest even integer smaller than num
     */
    private static int makeEven(int num) {

            /* The last bit determines +1 or +0, so unset it. There is no
               overflow for neither the integer minimum nor maximum. */
        return num & ~1;

    }

    /**
     * Represents an individual layer in the frame that can be written to and read from.
     * @author soir20
     */
    private interface Layer {

        /**
         * Writes a color to the specified point in this layer.
         * @param point     the point to write the color at
         * @param color     the color to write
         */
        void write(Point point, Color color);

        /**
         * Reads a color from the specified point in this layer.
         * @param point     the point to read from
         * @return the color at the given point
         */
        Color read(Point point);

    }

    /**
     * Represents the bottommost layer of a frame unless there is only one layer.
     * @author soir20
     */
    private static class BottomLayer implements Layer {
        private final TopLayer TOP_LAYER;
        private final byte INDEX;
        private final SparseIntMatrix POINTS;

        /**
         * Creates a new bottommost layer.
         * @param topLayer      topmost layer in this frame
         * @param width         width of this layer
         * @param height        height of this layer
         */
        public BottomLayer(TopLayer topLayer, int width, int height) {
            TOP_LAYER = topLayer;
            INDEX = 0;
            POINTS = new SparseIntMatrix(width, height, 3);
        }

        /**
         * Writes a color to the specified point in this layer. If the bottom layer has
         * already written to itself at the given point, then this method does nothing.
         * Does not attempt to write the color to the underlying image (the top layer).
         * @param point     the point to try to write to
         * @param color     the color to write at the given point
         */
        public void tryWrite(Point point, Color color) {
            if (!POINTS.isSet(point.x(), point.y())) {
                POINTS.set(point.x(), point.y(), color.combine());
            }
        }

        @Override
        public void write(Point point, Color color) {
            POINTS.set(point.x(), point.y(), color.combine());
            TOP_LAYER.tryWrite(point, color, INDEX);
        }

        @Override
        public Color read(Point point) {
            if (POINTS.isSet(point.x(), point.y())) {
                return new Color(POINTS.get(point.x(), point.y()));
            }

            return TOP_LAYER.read(point);
        }
    }

    /**
     * Represents layers of the frame that are neither bottommost nor topmost.
     * @author soir20
     */
    private static class MiddleLayer implements Layer {
        private final TopLayer TOP_LAYER;
        private final Layer LAYER_BELOW;
        private final byte INDEX;
        private final SparseIntMatrix POINTS;

        /**
         * Creates a new middle layer.
         * @param topLayer      topmost layer
         * @param layerBelow    layer below this layer
         * @param width         width of this layer
         * @param height        height of this layer
         * @param index         index of this layer
         */
        public MiddleLayer(TopLayer topLayer, Layer layerBelow, int width, int height, byte index) {
            TOP_LAYER = topLayer;
            LAYER_BELOW = layerBelow;
            INDEX = index;
            POINTS = new SparseIntMatrix(width, height, 3);
        }

        @Override
        public void write(Point point, Color color) {
            POINTS.set(point.x(), point.y(), color.combine());
            TOP_LAYER.tryWrite(point, color, INDEX);
        }

        @Override
        public Color read(Point point) {
            if (POINTS.isSet(point.x(), point.y())) {
                return new Color(POINTS.get(point.x(), point.y()));
            }
            return LAYER_BELOW.read(point);
        }
    }

    /**
     * Represents the topmost (and possibly bottommost) layer of the frame.
     * @author soir20
     */
    private static class TopLayer implements Layer {
        private final CloseableImage IMAGE;
        private final int WIDTH;
        private final byte INDEX;
        private final byte[] MODIFIED_BY;
        private final Queue<Point> LAST_MODIFIED;
        private BottomLayer bottomLayer;

        /**
         * Creates a new topmost layer.
         * @param image     the main image for the frame
         * @param width     the width of the frame
         * @param height    the height of the frame
         * @param index     the index of this layer
         */
        public TopLayer(CloseableImage image, int width, int height, byte index) {
            IMAGE = image;
            WIDTH = width;
            INDEX = index;
            MODIFIED_BY = new byte[width * height];
            LAST_MODIFIED = new ArrayDeque<>();
        }

        /**
         * Sets the bottommost layer of the same frame.
         * @param bottomLayer       the bottommost layer of the same frame
         */
        public void setBottomLayer(BottomLayer bottomLayer) {
            this.bottomLayer = bottomLayer;
        }

        /**
         * Retrieves the queue of points that have been modified in this layer. The returned
         * queue is mutable. Note that it is possible for the queue to contain duplicates.
         * @return the modified points in the topmost layer
         */
        public Queue<Point> lastModified() {
            return LAST_MODIFIED;
        }

        /**
         * Writes to this layer from a lower layer. If the top layer has already
         * written to itself at the given point, then this method does nothing.
         * @param point     the point to try to write to
         * @param color     the color to write at the given point
         * @param layer     the layer that is writing to this layer
         */
        public void tryWrite(Point point, Color color, byte layer) {
            int pointIndex = pointIndex(point);
            if (layer < MODIFIED_BY[pointIndex]) {
                return;
            }

            /* Attempting to write to the bottom layer handles three different cases:
               1. The bottom layer writes to the top layer. In this case, the bottom layer has already
                  written to itself, so writing to it again with tryWrite() does nothing.

               2. The top layer is written to before the bottom layer has been written to (at the
                  particular point). In this case, simply writing to the top layer will cause the
                  bottom layer to be modified as well. This is problematic because the bottom layer
                  uses the underlying image to retrieve points it does not already store. That means
                  writing to the top layer will effectively modify the bottom layer, while the goal of
                  the laying system is to have each layer only be aware of changes in the layers below
                  it. So we need to take the old color and move it to the bottom layer to maintain the
                  illusion that nothing has changed in the lower layers.

               3. The top layer is written to after the bottom layer has been written to (at the
                  particular point). Either the bottom layer is storing the original color (case 2),
                  or the original color has been overwritten at the bottom layer and is therefore
                  irrelevant. Hence, we don't need to write the original color to the bottom layer. */
            if (bottomLayer != null) {
                bottomLayer.tryWrite(point, read(point));
            }

            IMAGE.setColor(point.x(), point.y(), color.combine());
            MODIFIED_BY[pointIndex] = layer;
            LAST_MODIFIED.add(point);
        }

        @Override
        public void write(Point point, Color color) {
            tryWrite(point, color, INDEX);
        }

        @Override
        public Color read(Point point) {
            return new Color(IMAGE.color(point.x(), point.y()));
        }

        /**
         * Converts a point to the index of a point in the modified set.
         * @param point     the point to convert to an index
         * @return the index corresponding to this point
         */
        private int pointIndex(Point point) {
            return WIDTH * point.y() + point.x();
        }

    }

}
