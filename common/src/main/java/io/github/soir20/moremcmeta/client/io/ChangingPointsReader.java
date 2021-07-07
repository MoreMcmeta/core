/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.io;

import io.github.soir20.moremcmeta.client.texture.IRGBAImage;
import io.github.soir20.moremcmeta.math.Point;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Finds the pixels that change during an animation for an {@link IRGBAImage}.
 * @author soir20
 */
public class ChangingPointsReader {

    /**
     * Gets the pixels that will change for every mipmap of an image. If the image
     * or one of its mipmaps is empty, an empty area will be returned for that mipmap.
     * If the image dimensions are not multiples of the frame width or frame height,
     * the area on the right and bottom that are outside the frames will be ignored.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame. Must be greater than 0.
     * @param frameHeight   the height of a frame. Must be greater than 0.
     * @param mipmap        number of mipmap levels to use. Must be greater than or equal to 0.
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    public List<IRGBAImage.VisibleArea> read(IRGBAImage image, int frameWidth, int frameHeight, int mipmap) {
        requireNonNull(image, "Image cannot be null");
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Frames must not be empty");
        }
        if (mipmap < 0) {
            throw new IllegalArgumentException("Mipmap level cannot be less than zero");
        }

        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();

        int widthWithFrames = image.getWidth() / frameWidth * frameWidth;
        int heightWithFrames = image.getHeight() / frameHeight * frameHeight;

        // Find points in original image
        IRGBAImage.VisibleArea.Builder noMipmapBuilder = new IRGBAImage.VisibleArea.Builder();
        for (int y = 0; y < heightWithFrames; y++) {
            for (int x = 0; x < widthWithFrames; x++) {
                int frameX = x % frameWidth;
                int frameY = y % frameHeight;

                // We want to detect a point that changes in any frame
                if (!areColorsEqual(image.getPixel(x, y), image.getPixel(frameX, frameY))) {
                    noMipmapBuilder.addPixel(frameX, frameY);
                }

            }
        }
        visibleAreas.add(noMipmapBuilder.build());

        // Point coordinates will be different for all mipmap levels
        for (int level = 1; level <= mipmap; level++) {
            IRGBAImage.VisibleArea.Builder mipmapBuilder = new IRGBAImage.VisibleArea.Builder();

            if (frameWidth >> level > 0 & frameHeight >> level > 0) {
                for (Point point : visibleAreas.get(0)) {
                    mipmapBuilder.addPixel(point.getX() >> level, point.getY() >> level);
                }
            }

            visibleAreas.add(mipmapBuilder.build());
        }

        return visibleAreas;
    }

    /**
     * Determines if two RGBA colors are the same color. Equal colors have identical
     * alpha, red, blue, and green components or are both completely transparent
     * (alpha == 0).
     * @param firstColor        the first color to compare
     * @param secondColor       the second color to compare
     * @return whether the two colors are the same colors
     */
    private boolean areColorsEqual(int firstColor, int secondColor) {
        int firstAlpha = firstColor >> 24;
        int secondAlpha = secondColor >> 24;
        return firstColor == secondColor || (firstAlpha == 0 && secondAlpha == 0);
    }

}
