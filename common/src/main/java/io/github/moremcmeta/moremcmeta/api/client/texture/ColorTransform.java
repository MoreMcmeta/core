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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import io.github.moremcmeta.moremcmeta.api.math.Point;

import java.util.function.Function;

/**
 * Calculates a new RGBA color for an individual pixel in a frame.
 * @author soir20
 * @since 4.0.0
 */
@FunctionalInterface
public interface ColorTransform {

    /**
     * Calculates the new color of the pixel at the given coordinate in the frame being modified.
     * While this method only takes the x and y-coordinates of the pixel, a {@link FrameView} for
     * the frame being modified is available in contexts where a {@link ColorTransform} would be
     * provided.
     * @param overwritePoint        location of the pixel whose color will be replaced
     * @param dependencyFunction    function to retrieve the color of a dependency. If a point
     *                              not given as a dependency when this transform was applied is
     *                              requested, it will throw a {@link NonDependencyRequestException}.
     *                              The colors returned will be those before the associated transformation
     *                              was applied to any points.
     * @return the new color of the pixel at (x, y) in the format
     */
    Color transform(Point overwritePoint, Function<Point, Color> dependencyFunction);

    /**
     * Indicates that a transform requested the current color of a point that is not its dependency.
     * @author soir20
     * @since 4.0.0
     */
    class NonDependencyRequestException extends RuntimeException {

        /**
         * Creates a new exception to indicate a transform requested a non-dependency.
         */
        public NonDependencyRequestException() {
            super("A transform tried to retrieve the color of a point that is not its dependency");
        }

    }

}