package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;

import java.util.Set;

/**
 * An image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface IRGBAImage {

    /**
     * Gets the color of a pixel in this image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    int getPixel(int x, int y);

    /**
     * Sets the color of a pixel in this image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    void setPixel(int x, int y, int color);

    /**
     * Gets the width of this image.
     * @return  the width of this image
     */
    int getWidth();

    /**
     * Gets the height of this image.
     * @return  the height of this image
     */
    int getHeight();

    Set<Pair<Integer, Integer>> getInterpolatablePoints();

}
