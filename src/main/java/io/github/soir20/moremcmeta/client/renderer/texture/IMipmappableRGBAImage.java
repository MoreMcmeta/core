package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.Collection;

/**
 * A mipmapped image with an RGB color scheme.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @author soir20
 */
public interface IMipmappableRGBAImage {

    /**
     * Gets the color of a pixel in a mipmap.
     * @param level     the mipmap level
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    int getPixel(int level, int x, int y);

    /**
     * Sets the color of a pixel in a mipmap.
     * @param level     the mipmap level
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    void setPixel(int level, int x, int y, int color);

    /**
     * Gets the width of a mipmap.
     * @param level     the mipmap level
     * @return  the width of this image
     */
    int getWidth(int level);

    /**
     * Gets the height of a mipmap.
     * @param level     the mipmap level
     * @return  the height of this image
     */
    int getHeight(int level);

    boolean isMipmapped();

    Collection<Integer> getMipmapLevels();

}