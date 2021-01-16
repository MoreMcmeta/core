package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

/**
 * Wraps a {@link NativeImage} so it is compatible with the {@link IRGBAImage} interface and
 * the {@link RGBAInterpolator}.
 * @author soir20
 */
public class NativeImageRGBAWrapper implements IRGBAImage {
    private final NativeImage IMAGE;

    /**
     * Creates a new {@link NativeImage} wrapper.
     * @param image     the image to wrap
     */
    public NativeImageRGBAWrapper(NativeImage image) {
        IMAGE = image;
    }

    /**
     * Gets the color of a pixel in the image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    @Override
    public int getPixel(int x, int y) {
        return IMAGE.getPixelRGBA(x, y);
    }

    /**
     * Sets the color of a pixel in the image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    @Override
    public void setPixel(int x, int y, int color) {
        IMAGE.setPixelRGBA(x, y, color);
    }

    /**
     * Gets the width of the image.
     * @return  the width of the image
     */
    @Override
    public int getWidth() {
        return IMAGE.getWidth();
    }

    /**
     * Gets the height of the image.
     * @return  the height of the image
     */
    @Override
    public int getHeight() {
        return IMAGE.getHeight();
    }

}