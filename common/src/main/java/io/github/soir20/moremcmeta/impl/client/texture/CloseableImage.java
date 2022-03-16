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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface CloseableImage {

    /**
     * Gets the color of a pixel in this image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     * @throws IllegalStateException if this image has been closed
     */
    int getPixel(int x, int y);

    /**
     * Sets the color of a pixel in this image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     * @throws IllegalStateException if this image has been closed
     */
    void setPixel(int x, int y, int color);

    /**
     * Gets the width (pixels) of this image.
     * @return  the width of this image
     * @throws IllegalStateException if this image has been closed
     */
    int getWidth();

    /**
     * Gets the height (pixels) of this image.
     * @return  the height of this image
     * @throws IllegalStateException if this image has been closed
     */
    int getHeight();

    /**
     * Gets the visible area (iterable by point) of this image. Order of points is not guaranteed.
     * @return  the visible area of this image
     * @throws IllegalStateException if this image has been closed
     */
    VisibleArea getVisibleArea();

    /**
     * Uploads the top-left corner of this image at the given coordinates.
     * @param uploadX       horizontal position to upload at
     * @param uploadY       vertical position to upload at
     * @throws IllegalStateException if this image has been closed
     */
    void upload(int uploadX, int uploadY);

    /**
     * Closes any resources associated with this image. Implementations should be idempotent.
     *
     * Currently, no image implementations need to throw exceptions, and {@link AutoCloseable} is not
     * idempotent. An image is not an I/O resource like {@link java.io.Closeable}. Hence, this interface
     * has its own close() method instead of extending one of the existing closeable interfaces.
     */
    void close();

    /**
     * Represents a collection of unordered visible points in an image. Use this to ignore parts of an image
     * in speed-sensitive areas like rendering. Colored points can be ignored by not adding them, as well;
     * the color and opacity of added pixels are not enforced.
     *
     * @author soir20
     */
    class VisibleArea implements Iterable<Point> {
        private final Set<VisibleRow> VISIBLE_ROWS;

        /**
         * Gets the iterator for all the points in this area. The points are not in a guaranteed order.
         *
         * @return the iterator for all points in this area
         */
        @Override
        public Iterator<Point> iterator() {
            return new VisiblePointIterator(VISIBLE_ROWS);
        }

        /**
         * Builds a new, immutable visible area.
         *
         * @author soir20
         */
        public static class Builder {

            // Keys are y (row) coordinates. Values are x (column) coordinates.
            private final Map<Integer, Set<Integer>> ROWS;

            /**
             * Creates a new builder for a visible area.
             */
            public Builder() {
                ROWS = new HashMap<>();
            }

            /**
             * Adds a visible pixel to the area.
             *
             * @param x x-coordinate of the pixel
             * @param y y-coordinate of the pixel
             */
            public void addPixel(int x, int y) {
                if (!ROWS.containsKey(y)) {
                    ROWS.put(y, new HashSet<>());
                }

                ROWS.get(y).add(x);
            }

            /**
             * Builds the visible area based on the provided points.
             *
             * @return the visible area
             */
            public VisibleArea build() {
                Set<VisibleRow> visibleRows = new HashSet<>();

                for (Map.Entry<Integer, Set<Integer>> entry : ROWS.entrySet()) {
                    List<Integer> xPoints = entry.getValue().stream().sorted().toList();
                    int numPoints = xPoints.size();

                    int startIndex = 0;
                    for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                        int nextIndex = pointIndex + 1;

                        boolean isLastPoint = pointIndex == numPoints - 1;
                        boolean isRowEnd = isLastPoint || xPoints.get(nextIndex) - xPoints.get(pointIndex) > 1;

                        if (isRowEnd) {
                            int width = pointIndex - startIndex + 1;
                            visibleRows.add(new VisibleRow(xPoints.get(startIndex), entry.getKey(), width));

                            startIndex = nextIndex;
                        }
                    }
                }

                return new VisibleArea(visibleRows);
            }

        }

        /**
         * Creates a new visible area.
         *
         * @param rows all of the visible horizontal strips in this image
         */
        private VisibleArea(Set<VisibleRow> rows) {
            VISIBLE_ROWS = rows;
        }

        /**
         * Represents continuous, one-pixel-high horizontal strips in an image.
         *
         * @author soir20
         */
        private static class VisibleRow {
            private final int X;
            private final int Y;
            private final int WIDTH;

            /**
             * Creates a new visible row in this image.
             *
             * @param x     left x-coordinate of the row
             * @param y     left y-coordinate of the row
             * @param width width of the row in pixels
             */
            public VisibleRow(int x, int y, int width) {
                X = x;
                Y = y;
                WIDTH = width;
            }

            /**
             * Gets the hash code of this row.
             *
             * @return the hash code of this row
             */
            @Override
            public int hashCode() {
                return Objects.hash(X, Y, WIDTH);
            }

            /**
             * Determines if this row is the same as another object.
             *
             * @param other the other object to test
             * @return whether the two objects are identical rows
             */
            @Override
            public boolean equals(Object other) {
                if (!(other instanceof VisibleRow otherRow)) {
                    return false;
                }

                return X == otherRow.X && Y == otherRow.Y && WIDTH == ((VisibleRow) other).WIDTH;
            }

        }

        /**
         * Iterates over all the points in a {@link VisibleArea}.
         *
         * @author soir20
         */
        private static class VisiblePointIterator implements Iterator<Point> {
            private final Iterator<VisibleRow> rowIterator;
            private VisibleRow currentRow;
            private int pixelCount;

            /**
             * Creates a new iterator.
             *
             * @param rows all the visible rows in the visible area
             */
            public VisiblePointIterator(Set<VisibleRow> rows) {
                rowIterator = rows.iterator();
            }

            /**
             * Determines whether there are any points left to iterate over.
             *
             * @return whether this iterator has any remaining points
             */
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext() || (currentRow != null && pixelCount < currentRow.WIDTH);
            }

            /**
             * Gets the next point in a visible area. Order is not guaranteed.
             *
             * @return the next point in a visible area
             */
            @Override
            public Point next() {
                if (currentRow == null || pixelCount == currentRow.WIDTH) {
                    currentRow = rowIterator.next();
                    pixelCount = 0;
                }

                pixelCount++;
                return new Point(currentRow.X + pixelCount - 1, currentRow.Y);
            }

        }

    }
}
