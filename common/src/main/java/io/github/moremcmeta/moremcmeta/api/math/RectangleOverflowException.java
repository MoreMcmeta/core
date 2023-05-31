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
 * Indicates that a rectangle was created that contains points that cannot be represented with
 * two 32-bit integers.
 * @author soir20
 * @since 4.0.0
 */
public final class RectangleOverflowException extends IllegalArgumentException {

    /**
     * Creates a new exception to indicate that a rectangle was created that contains points
     * that cannot be represented with two 32-bit integers.
     * @param x      x-coordinate of the top-left corner of the rectangle
     * @param y      y-coordinate of the top-right corner of the rectangle
     * @param width  width of the rectangle
     * @param height height of the rectangle
     */
    public RectangleOverflowException(int x, int y, int width, int height) {
        super("Points in the " + width + "x" + height + " rectangle starting at ("
                + x + ", " + y + ") cannot be represented by 32-bit coordinates");
    }

}
