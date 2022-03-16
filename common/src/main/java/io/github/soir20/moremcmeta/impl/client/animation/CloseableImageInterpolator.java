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

package io.github.soir20.moremcmeta.impl.client.animation;

import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.api.math.Point;

import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

/**
 * Generates interpolated images in between two other images.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public class CloseableImageInterpolator implements Interpolator<CloseableImage> {
    private final BiFunction<Integer, Integer, ? extends CloseableImage> IMAGE_GETTER;

    /**
     * Creates a new interpolator.
     * @param imageGetter      accepts a width and height to produce interpolated images.
     *                         Colors are overwritten. The longest width and the longest height
     *                         are selected between the start and end images.
     */
    public CloseableImageInterpolator(BiFunction<Integer, Integer, ? extends CloseableImage> imageGetter) {
        IMAGE_GETTER = requireNonNull(imageGetter, "CloseableImage getter cannot be null");
    }

    /**
     * Generates an interpolate images between two images.
     * @param steps     the number of steps it should take from the start image to reach the end image
     * @param step      the current step in the interpolation. Start at 1, and end at steps - 1.
     * @param start     the image to start interpolation at (not returned)
     * @param end       the image to end interpolation at (not returned)
     * @return  the interpolated frame at this step
     */
    public CloseableImage interpolate(int steps, int step, CloseableImage start, CloseableImage end) {
        if (step < 1 || step >= steps) {
            throw new IllegalArgumentException("Step must be between 1 and steps - 1 (inclusive)");
        }

        requireNonNull(start, "Start frame cannot be null");
        requireNonNull(end, "End frame cannot be null");

        double ratio = 1.0 - (step / (double) steps);
        return mixCloseableImage(ratio, start, end);
    }

    /**
     * Mixes the colors in two images to produce a new image.
     * @param startProportion   proportion of start color to mix (1 - proportion of end color)
     * @param start             start image (unchanged)
     * @param end               end image (unchanged)
     * @return  the image with mixed colors (using the largest dimension in each direction)
     */
    private CloseableImage mixCloseableImage(double startProportion, CloseableImage start, CloseableImage end) {
        int maxWidth = Math.max(start.getWidth(), end.getWidth());
        int maxHeight = Math.max(start.getHeight(), end.getHeight());

        CloseableImage output = IMAGE_GETTER.apply(maxWidth, maxHeight);
        requireNonNull(output, "Interpolated image was created as null");

        CloseableImage.VisibleArea points = output.getVisibleArea();
        for (Point point : points) {
            int xPos = point.getX();
            int yPos = point.getY();

            int startColor = getPixel(start, xPos, yPos);
            int endColor = getPixel(end, xPos, yPos);
            int mixedColor = mixPixel(startProportion, startColor, endColor);

            output.setPixel(xPos, yPos, mixedColor);
        }

        return output;
    }

    /**
     * Gets the color of a pixel at a coordinate, or a transparent pixel if the image has no pixel there.
     * @param image     image to retrieve pixel from
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @return  the color of the pixel or a transparent pixel
     */
    private int getPixel(CloseableImage image, int x, int y) {
        if (x < image.getWidth() && y < image.getHeight()) {
            return image.getPixel(x, y);
        } else {
            return 0;
        }
    }

    /**
     * Mixes the colors of two pixels into a single color.
     * @param startProportion   proportion of start color to mix (1 - proportion of end color)
     * @param startColor        color of the first pixel
     * @param endColor          color of the second pixel
     * @return  the resultant mixed color
     */
    private int mixPixel(double startProportion, int startColor, int endColor) {
        int red = mixComponent(startProportion,
                extractRed(startColor), extractRed(endColor));
        int green = mixComponent(startProportion,
                extractGreen(startColor), extractGreen(endColor));
        int blue = mixComponent(startProportion,
                extractBlue(startColor), extractBlue(endColor));

        return startColor & 0b11111111000000000000000000000000 | red << 16 | green << 8 | blue;
    }

    /**
     * Mixes one component from two RGB colors.
     * @param startProportion   proportion of start color to mix (1 - proportion of end color)
     * @param startColor        value of the first color's component
     * @param endColor          value of the second color's component
     * @return  the resultant mixed component
     */
    private int mixComponent(double startProportion, int startColor, int endColor) {
        return (int) (startProportion * startColor + (1.0 - startProportion) * endColor);
    }

    /**
     * Extracts the value of the red component from an RGB color.
     * @param color         the RGB color to extract from
     * @return  the value of the red component in this color
     */
    private int extractRed(int color) {
        return color >> 16 & 255;
    }

    /**
     * Extracts the value of the green component from an RGB color.
     * @param color         the RGB color to extract from
     * @return  the value of the green component in this color
     */
    private int extractGreen(int color) {
        return color >> 8 & 255;
    }

    /**
     * Extracts the value of the blue component from an RGB color.
     * @param color         the RGB color to extract from
     * @return  the value of the blue component in this color
     */
    private int extractBlue(int color) {
        return color & 255;
    }

}
