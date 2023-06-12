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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import io.github.moremcmeta.moremcmeta.impl.client.texture.ColorBlender;

/**
 * Represents an RGBA color.
 * @author soir20
 * @since 4.0.0
 */
public final class Color {
    private final static int COMPONENT_MASK = 0xff;
    private final static int RED_OFFSET = 16;
    private final static int GREEN_OFFSET = 8;
    private final static int BLUE_OFFSET = 0;
    private final static int ALPHA_OFFSET = 24;

    /**
     * Creates a new color using individual components.
     * @param red       the red component of the color, must be in [0, 255]
     * @param green     the green component of the color, must be in [0, 255]
     * @param blue      the blue component of the color, must be in [0, 255]
     * @param alpha     the alpha component of the color, must be in [0, 255]
     * @return an integer representing the 32-bit color
     */
    public static int pack(int red, int green, int blue, int alpha) {
        checkComponent(red);
        checkComponent(green);
        checkComponent(blue);
        checkComponent(alpha);
        return (alpha << ALPHA_OFFSET) | (red << RED_OFFSET) | (green << GREEN_OFFSET) | (blue << BLUE_OFFSET);
    }

    /**
     * Gets the red component of the color.
     * @param color     the color to get the component of
     * @return red component of the color
     */
    public static int red(int color) {
        return (color >> RED_OFFSET) & COMPONENT_MASK;
    }

    /**
     * Gets the green component of the color.
     * @param color     the color to get the component of
     * @return green component of the color
     */
    public static int green(int color) {
        return (color >> GREEN_OFFSET) & COMPONENT_MASK;
    }

    /**
     * Gets the blue component of the color.
     * @param color     the color to get the component of
     * @return blue component of the color
     */
    public static int blue(int color) {
        return (color >> BLUE_OFFSET) & COMPONENT_MASK;
    }

    /**
     * Gets the blue component of the color.
     * @param color     the color to get the component of
     * @return blue component of the color
     */
    public static int alpha(int color) {
        return (color >> ALPHA_OFFSET) & COMPONENT_MASK;
    }

    /**
     * Performs alpha blending/compositing between two partially-transparent colors, where one color
     * is on top of another color.
     * @param topColor          color above the other color
     * @param bottomColor       color below the other color
     * @return alpha-blended color when the topColor is above the bottomColor in the same pixel
     */
    public static int alphaBlend(int topColor, int bottomColor) {
        return ColorBlender.alphaBlend(topColor, bottomColor);
    }

    /**
     * Check whether this color is the same as another color. Two colors are considered equal if
     * they are both fully transparent, even if their RGB components differ.
     * @param color1        the first color to compare
     * @param color2        the second color to compare
     * @return true if the other object is a color with the same components or false otherwise
     */
    public static boolean equalsOrBothInvisible(int color1, int color2) {
        return color1 == color2 || (alpha(color1) == 0 && alpha(color2) == 0);
    }

    /**
     * Prevents a Color from being constructed.
     */
    private Color() {}

    /**
     * Checks that an individual component is in the [0, 255] range.
     * @param component the component to check
     * @throws IllegalRGBAComponentException if the component is outside the required range
     */
    private static void checkComponent(int component) {
        if (component < 0 || component > 255) {
            throw new IllegalRGBAComponentException(component);
        }
    }

}
