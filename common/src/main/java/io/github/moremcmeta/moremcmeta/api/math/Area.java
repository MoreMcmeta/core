/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
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

package io.github.moremcmeta.moremcmeta.api.math;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Represents an unordered collection of points.
 * @author soir20
 * @since 4.0.0
 */
public final class Area implements LongIterable {

    /**
     * Creates an area from a predefined list of points.
     * @param points        the points that make up the area
     * @return an area containing only these points
     */
    public static Area of(long... points) {
        Area.Builder builder = new Builder();

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        return builder.build();
    }

    /**
     * Creates an area from a predefined list of points.
     * @param points        the points that make up the area
     * @return an area containing only these points
     */
    public static Area of(Iterable<Long> points) {
        Area.Builder builder = new Builder();

        for (long point : points) {
            builder.addPixel(Point.x(point), Point.y(point));
        }

        return builder.build();
    }

    // Each long list represents a segment of (leftX, segmentWidth) pairs
    private final Int2ObjectSortedMap<LongList> ROWS;

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

        ROWS = new Int2ObjectRBTreeMap<>();

        if (width > 0) {
            for (int y = topLeftY; y < topLeftY + height; y++) {
                ROWS.put(y, new LongImmutableList(ImmutableList.of(Point.pack(topLeftX, width))));
            }
        }
    }

    /**
     * Gets the iterator for all the points in this area. The points are not in a guaranteed order.
     * @return the iterator for all points in this area
     */
    @Override
    public @NotNull LongIterator iterator() {
        return new PointIterator();
    }

    /**
     * Builds a new, immutable area.
     * @author soir20
     * @since 4.0.0
     */
    public static final class Builder {

        // Keys are y (row) coordinates. Values are x (column) coordinates.
        private final Int2ObjectSortedMap<IntList> ROWS;

        /**
         * Creates a new builder for an area.
         */
        public Builder() {
            ROWS = new Int2ObjectRBTreeMap<>();
        }

        /**
         * Adds a pixel to the area.
         * @param x     x-coordinate of the pixel
         * @param y     y-coordinate of the pixel
         */
        public void addPixel(int x, int y) {
            if (!ROWS.containsKey(y)) {
                ROWS.put(y, new IntArrayList());
            }

            ROWS.get(y).add(x);
        }

        /**
         * Adds a pixel to the area.
         * @param point     coordinate of the pixel to add
         */
        public void addPixel(long point) {
            addPixel(Point.x(point), Point.y(point));
        }

        /**
         * Builds the area based on the provided points.
         * @return  the area
         */
        public Area build() {
            Int2ObjectSortedMap<LongList> rows = new Int2ObjectRBTreeMap<>();

            for (Int2ObjectMap.Entry<IntList> entry : ROWS.int2ObjectEntrySet()) {
                IntList xPoints = entry.getValue();
                xPoints.sort(Integer::compare);
                int numPoints = xPoints.size();

                int startIndex = 0;
                for (int pointIndex = 0; pointIndex < numPoints; pointIndex++) {
                    int nextIndex = pointIndex + 1;

                    boolean isLastPoint = pointIndex == numPoints - 1;
                    boolean isRowEnd = isLastPoint || xPoints.getInt(nextIndex) - xPoints.getInt(pointIndex) > 1;

                    if (isRowEnd) {
                        int width = pointIndex - startIndex + 1;
                        int y = entry.getIntKey();
                        rows.computeIfAbsent(y, (key) -> new LongArrayList())
                                .add(Point.pack(xPoints.getInt(startIndex), width));

                        startIndex = nextIndex;
                    }
                }
            }

            return new Area(rows);
        }

    }

    /**
     * Creates a new area.
     * @param rows  all the horizontal strips in this image
     */
    private Area(Int2ObjectSortedMap<LongList> rows) {
        ROWS = rows;
    }

    /**
     * Iterates over all the points in a {@link Area}.
     * @author soir20
     */
    private class PointIterator implements LongIterator {
        private final Iterator<Int2ObjectMap.Entry<LongList>> ROW_ITERATOR = ROWS.int2ObjectEntrySet().iterator();
        private int currentRowY;
        private LongIterator segmentIterator;
        private int currentSegmentX;
        private int currentSegmentWidth;
        private int pixelCount;

        /**
         * Creates a new iterator.
         */
        public PointIterator() {
            segmentIterator = LongIterators.EMPTY_ITERATOR;
        }

        @Override
        public boolean hasNext() {
            return ROW_ITERATOR.hasNext() || segmentIterator.hasNext() || pixelCount < currentSegmentWidth;
        }

        /**
         * Gets the next point in an area. Order is not guaranteed.
         * @return  the next point in an area
         */
        @Override
        public long nextLong() {
            boolean atSegmentEnd = pixelCount == currentSegmentWidth;

            if (atSegmentEnd && !segmentIterator.hasNext()) {
                Int2ObjectMap.Entry<LongList> currentRow = ROW_ITERATOR.next();
                segmentIterator = currentRow.getValue().iterator();
                currentRowY = currentRow.getIntKey();
            }

            if (atSegmentEnd) {
                long currentSegment = segmentIterator.nextLong();
                currentSegmentX = Point.x(currentSegment);
                currentSegmentWidth = Point.y(currentSegment);
                pixelCount = 0;
            }

            pixelCount++;
            return Point.pack(currentSegmentX + pixelCount - 1, currentRowY);
        }

    }

}