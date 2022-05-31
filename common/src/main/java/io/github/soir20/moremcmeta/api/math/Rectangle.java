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

package io.github.soir20.moremcmeta.api.math;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a rectangular area, all of whose points can be iterated.
 * @author soir20
 * @since 4.0.0
 */
public final class Rectangle implements Iterable<Point> {
    private final int X_START;
    private final int Y_START;
    private final int WIDTH;
    private final int HEIGHT;

    /**
     * Creates a new rectangular area.
     * @param topLeftX      x-coordinate of the top-left point of the rectangle
     * @param topLeftY      y-coordinate of the top-right point of the rectangle
     * @param width         width of the rectangle
     * @param height        height of the rectangle
     * @throws NegativeDimensionException if width or height is negative
     * @throws RectangleOverflowException if topLeftX + width > INT_MAX or topLeftY + height > INT_MAX
     */
    public Rectangle(int topLeftX, int topLeftY, int width, int height) {
        if (width < 0) {
            throw new NegativeDimensionException(width);
        }

        if (height < 0) {
            throw new NegativeDimensionException(height);
        }

        if (Integer.MAX_VALUE - width < topLeftX || Integer.MAX_VALUE - height < topLeftY) {
            throw new RectangleOverflowException(topLeftX, topLeftY, width, height);
        }

        X_START = topLeftX;
        Y_START = topLeftY;
        WIDTH = width;
        HEIGHT = height;
    }

    /**
     * Gets an iterator that visits every point in this rectangle once.
     * @return an iterator for all points in the rectangle
     */
    @NotNull
    @Override
    public Iterator<Point> iterator() {
        return new RectangleIterator();
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
     * Iterates over all points in this rectangle exactly once.
     * @author soir20
     */
    private class RectangleIterator implements Iterator<Point> {
        private int currentRow;
        private int currentCol;

        /**
         * Creates a new iterator for this rectangle.
         */
        public RectangleIterator() {
            currentRow = 0;
            currentCol = 0;
        }

        /**
         * Checks if this iterator has another point.
         * @return true if this iterator has another point, otherwise false
         */
        @Override
        public boolean hasNext() {
            return currentCol < WIDTH && currentRow < HEIGHT;
        }

        /**
         * Gets the next point from this iterator. No order of points is guaranteed.
         * @return a point in the rectangle that this iterator has not visited
         */
        @Override
        public Point next() {
            int x = X_START + currentRow++;
            int y = Y_START + currentCol++;

            if (!hasNext()) {
                throw new NoSuchElementException("Rectangle has no more points");
            }

            return new Point(x, y);
        }

    }

}