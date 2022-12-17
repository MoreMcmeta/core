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

import java.util.Objects;

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

    private final int RED;
    private final int GREEN;
    private final int BLUE;
    private final int ALPHA;
    private final int COMBINED;

    /**
     * Creates a new color using individual components.
     * @param red       the red component of the color, must be in [0, 255]
     * @param green     the green component of the color, must be in [0, 255]
     * @param blue      the blue component of the color, must be in [0, 255]
     * @param alpha     the alpha component of the color, must be in [0, 255]
     */
    public Color(int red, int green, int blue, int alpha) {
        RED = checkComponent(red);
        GREEN = checkComponent(green);
        BLUE = checkComponent(blue);
        ALPHA = checkComponent(alpha);
        COMBINED = (ALPHA << ALPHA_OFFSET) | (RED << RED_OFFSET) | (GREEN << GREEN_OFFSET) | (BLUE << BLUE_OFFSET);
    }

    /**
     * Creates a new color from the AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB (32 bits) combined format.
     * @param combined      the color in combined format
     */
    public Color(int combined) {
        RED = (combined >> RED_OFFSET) & COMPONENT_MASK;
        GREEN = (combined >> GREEN_OFFSET) & COMPONENT_MASK;
        BLUE = (combined >> BLUE_OFFSET) & COMPONENT_MASK;
        ALPHA = (combined >> ALPHA_OFFSET) & COMPONENT_MASK;
        COMBINED = combined;
    }

    /**
     * Gets the red component of the color.
     * @return red component of the color
     */
    public int red() {
        return RED;
    }

    /**
     * Gets the green component of the color.
     * @return green component of the color
     */
    public int green() {
        return GREEN;
    }

    /**
     * Gets the blue component of the color.
     * @return blue component of the color
     */
    public int blue() {
        return BLUE;
    }

    /**
     * Gets the blue component of the color.
     * @return blue component of the color
     */
    public int alpha() {
        return ALPHA;
    }

    /**
     * Gets the color in the AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB (32 bits) combined format.
     * @return the color in combined format
     */
    public int combine() {
        return COMBINED;
    }

    /**
     * Check whether this color is the same as another object. This method is identical to {@link #equals(Object)},
     * but two {@link Color}s are considered equal if they are both fully transparent, even if their RGB components
     * differ.
     * @param other     the object to compare this color to
     * @return true if the other object is a color with the same components or false otherwise
     */
    public boolean equalsOrBothInvisible(Object other) {
        if (!(other instanceof Color otherAsColor)) {
            return false;
        }

        return (alpha() == 0 && otherAsColor.alpha() == 0) || (alpha() == otherAsColor.alpha()
                && red() == otherAsColor.red()
                && green() == otherAsColor.green()
                && blue() == otherAsColor.blue());
    }

    /**
     * Check whether this color is the same as another object.
     * @param other     the object to compare this color to
     * @return true if the other object is a color with the same components or false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Color otherAsColor)) {
            return false;
        }

        return alpha() == otherAsColor.alpha()
                && red() == otherAsColor.red()
                && green() == otherAsColor.green()
                && blue() == otherAsColor.blue();
    }

    /**
     * Gets the hash code for this color.
     * @return the hash code for this color
     */
    @Override
    public int hashCode() {
        return Objects.hash(ALPHA, RED, GREEN, BLUE);
    }

    /**
     * Checks that an individual component is in the [0, 255] range.
     * @param component     the component to check
     * @return the component if it is valid
     * @throws IllegalRGBAComponentException if the component is outside the required range
     */
    private static int checkComponent(int component) {
        if (component < 0 || component > 255) {
            throw new IllegalRGBAComponentException(component);
        }

        return component;
    }

    /**
     * Indicates that a provided color component is outside the required [0, 255] range.
     * @author soir20
     * @since 4.0.0
     */
    public static final class IllegalRGBAComponentException extends IllegalArgumentException {

        /**
         * Creates a new exception to indicate that a provided color component is outside
         * the required [0, 255] range.
         * @param component     the value of the component outside the required range
         */
        public IllegalRGBAComponentException(int component) {
            super("RGBA component " + component + " is outside [0, 255]");
        }

    }

}
