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

package io.github.moremcmeta.moremcmeta.api.math;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Represents an unordered collection of points.
 * @author soir20
 * @since 4.0.0
 */
public final class Area implements Iterable<Point> {

    /**
     * Creates an area from a predefined list of points.
     * @param points        the points that make up the area
     * @return an area containing only these points
     */
    public static Area of(Point... points) {
        Area.Builder builder = new Builder();

        for (Point point : points) {
            builder.addPixel(point.x(), point.y());
        }

        return builder.build();
    }

    /**
     * Creates an area from a predefined list of points.
     * @param points        the points that make up the area
     * @return an area containing only these points
     */
    public static Area of(Iterable<Point> points) {
        Area.Builder builder = new Builder();

        for (Point point : points) {
            builder.addPixel(point.x(), point.y());
        }

        return builder.build();
    }

    private final Set<Row> ROWS;

    /**
     * Creates a new area that represents a rectangle.
     * @param topLeftX      x-coordinate of the top-left point of the rectangle
     * @param topLeftY      y-coordinate of the top-right point of the rectangle
     * @param width         width of the rectangle
     * @param height        height of the rectangle
     * @throws NegativeDimensionException if width or height is negative
     * @throws RectangleOverflowException if topLeftX + width > INT_MAX or topLeftY + height > INT_MAX
     */
    public Area(int topLeftX, int topLeftY, int width, int height) {
        if (width < 0) {
            throw new NegativeDimensionException(width);
        }

        if (height < 0) {
            throw new NegativeDimensionException(height);
        }

        if (Integer.MAX_VALUE - width < topLeftX || Integer.MAX_VALUE - height < topLeftY) {
            throw new RectangleOverflowException(topLeftX, topLeftY, width, height);
        }

        ROWS = new HashSet<>();

        if (width > 0) {
            for (int y = topLeftY; y < topLeftY + height; y++) {
                ROWS.add(new Row(topLeftX, y, width));
            }
        }
    }

    /**
     * Gets the iterator for all the points in this area. The points are not in a guaranteed order.
     * @return  the iterator for all points in this area
     */
    @Override
    @NotNull
    public Iterator<Point> iterator() {
        return new PointIterator(ROWS);
    }

    /**
     * Builds a new, immutable area.
     * @author soir20
     * @since 4.0.0
     */
    public static final class Builder {

        // Keys are y (row) coordinates. Values are x (column) coordinates.
        private final Map<Integer, Set<Integer>> ROWS;

        /**
         * Creates a new builder for an area.
         */
        public Builder() {
            ROWS = new HashMap<>();
        }

        /**
         * Adds a pixel to the area.
         * @param x     x-coordinate of the pixel
         * @param y     y-coordinate of the pixel
         */
        public void addPixel(int x, int y) {
            if (!ROWS.containsKey(y)) {
                ROWS.put(y, new HashSet<>());
            }

            ROWS.get(y).add(x);
        }

        /**
         * Adds a pixel to the area.
         * @param point     coordinate of the pixel to add
         */
        public void addPixel(Point point) {
            requireNonNull(point, "Point cannot be null");
            addPixel(point.x(), point.y());
        }

        /**
         * Builds the area based on the provided points.
         * @return  the area
         */
        public Area build() {
            Set<Row> rows = new HashSet<>();

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
                        rows.add(new Row(xPoints.get(startIndex), entry.getKey(), width));

                        startIndex = nextIndex;
                    }
                }
            }

            return new Area(rows);
        }

    }

    /**
     * Indicates that a rectangle was created that contains points that cannot be represented with
     * two 32-bit integers.
     * @author soir20
     * @since 4.0.0
     */
    public static final class RectangleOverflowException extends IllegalArgumentException {

        /**
         * Creates a new exception to indicate that a rectangle was created that contains points
         * that cannot be represented with two 32-bit integers.
         * @param x         x-coordinate of the top-left corner of the rectangle
         * @param y         y-coordinate of the top-right corner of the rectangle
         * @param width     width of the rectangle
         * @param height    height of the rectangle
         */
        public RectangleOverflowException(int x, int y, int width, int height) {
            super("Points in the " + width + "x" + height + " rectangle starting at ("
                    + x + ", " + y + ") cannot be represented by 32-bit coordinates");
        }

    }

    /**
     * Creates a new area.
     * @param rows  all the horizontal strips in this image
     */
    private Area(Set<Row> rows) {
        ROWS = rows;
    }

    /**
     * Represents continuous, one-pixel-high horizontal strips in an image.
     * @author soir20
     */
    private static class Row {
        private final int X;
        private final int Y;
        private final int WIDTH;

        /**
         * Creates a new row in this image.
         * @param x         left x-coordinate of the row
         * @param y         left y-coordinate of the row
         * @param width     width of the row in pixels
         */
        public Row(int x, int y, int width) {
            X = x;
            Y = y;
            WIDTH = width;
        }

        /**
         * Gets the hash code of this row.
         * @return  the hash code of this row
         */
        @Override
        public int hashCode() {
            return Objects.hash(X, Y, WIDTH);
        }

        /**
         * Determines if this row is the same as another object.
         * @param other     the other object to test
         * @return  whether the two objects are identical rows
         */
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Row otherRow)) {
                return false;
            }

            return X == otherRow.X && Y == otherRow.Y && WIDTH == ((Row) other).WIDTH;
        }

    }

    /**
     * Iterates over all the points in a {@link Area}.
     * @author soir20
     */
    private static class PointIterator implements Iterator<Point> {
        private final Iterator<Row> rowIterator;
        private Row currentRow;
        private int pixelCount;

        /**
         * Creates a new iterator.
         * @param rows      all the rows in the area
         */
        public PointIterator(Set<Row> rows) {
            rowIterator = rows.iterator();
        }

        /**
         * Determines whether there are any points left to iterate over.
         * @return  whether this iterator has any remaining points
         */
        @Override
        public boolean hasNext() {
            return rowIterator.hasNext() || (currentRow != null && pixelCount < currentRow.WIDTH);
        }

        /**
         * Gets the next point in an area. Order is not guaranteed.
         * @return  the next point in an area
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