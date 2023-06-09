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

package io.github.moremcmeta.moremcmeta.api.math;

/**
 * A pair of coordinates in a plane.
 * @author soir20
 * @since 4.0.0
 */
public final class Point {
    private static final long COORD_MASK = 0b11111111_11111111_11111111_11111111L;
    private static final int X_OFFSET = 32;

    /**
     * Packs a point into a long primitive.
     * @param x     x-coordinate of the point
     * @param y     y-coordinate of the point
     * @return the point as a single long
     */
    public static long pack(int x, int y) {
        return ((long) x << X_OFFSET) | (y & COORD_MASK);
    }

    /**
     * Gets the horizontal coordinate of the point.
     * @param point     point to retrieve the x-coordinate of
     * @return x-coordinate of the point
     */
    public static int x(long point) {
        return (int) (point >> X_OFFSET);
    }

    /**
     * Gets the vertical coordinate of the point.
     * @param point     point to retrieve the y-coordinate of
     * @return y-coordinate of the point
     */
    public static int y(long point) {
        return (int) point;
    }

    /**
     * Converts this point to a string in the form (x, y).
     * @param point     point to convert
     * @return this point as a string
     */
    public static String toString(long point) {
        return "(" + Point.x(point) + ", " + Point.y(point) + ")";
    }

    /**
     * Prevents Point from being constructed.
     */
    private Point() {}

}
