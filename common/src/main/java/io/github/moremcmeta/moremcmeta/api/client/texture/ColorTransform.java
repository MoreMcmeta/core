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
     * @param overwriteX            x-coordinate of the location of the pixel whose color will be replaced
     * @param overwriteY            y-coordinate of the location of the pixel whose color will be replaced
     * @param layerBelow            retrieves the color of pixels in the layer below this plugin's layer.
     *                              Use this to write plugins that compute colors as a function of the
     *                              previous layers (say, grayscale filter that respects animations below).
     *                              If this plugin is the first one to be applied, then the layer below is
     *                              the original image.
     * @return the new color of the pixel at (x, y) in the format
     */
    int transform(int overwriteX, int overwriteY, LayerBelow layerBelow);

    /**
     * In a frame, the layer below this plugin's layer.
     * @author soir20
     * @since 4.0.0
     */
    interface LayerBelow {

        /**
         * Retrieves the color of the pixel at the given point.
         * @param x     x-coordinate of the pixel to retrieve the color of
         * @param y     y-coordinate of the pixel to retrieve the color of
         * @return the color at the given point
         */
        int color(int x, int y);

    }

}
