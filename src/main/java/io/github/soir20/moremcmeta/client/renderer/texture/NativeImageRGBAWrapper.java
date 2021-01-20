package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.NativeImage;

import java.util.Set;

/**
 * Wraps a {@link NativeImage} so it is compatible with the {@link IRGBAImage} interface and
 * the {@link RGBAInterpolator}.
 * @author soir20
 */
public class NativeImageRGBAWrapper implements IRGBAImage {
    private final NativeImage IMAGE;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int WIDTH;
    private final int HEIGHT;
    private final Set<Pair<Integer, Integer>> INTERPOLATABLE_POINTS;

    /**
     * Creates a new {@link NativeImage} wrapper.
     * @param image     the image to wrap
     */
    public NativeImageRGBAWrapper(NativeImage image, int xOffset, int yOffset, int width, int height,
                                  Set<Pair<Integer, Integer>> interpolatablePoints) {
        IMAGE = image;
        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
        WIDTH = width;
        HEIGHT = height;
        INTERPOLATABLE_POINTS = interpolatablePoints;
    }

    /**
     * Gets the color of a pixel in the image.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return  the color of the given pixel
     */
    @Override
    public int getPixel(int x, int y) {
        return IMAGE.getPixelRGBA(x + X_OFFSET, y + Y_OFFSET);
    }

    /**
     * Sets the color of a pixel in the image.
     * @param x         x-coordinate of the pixel
     * @param y         y-coordinate of the pixel
     * @param color     new color of the pixel
     */
    @Override
    public void setPixel(int x, int y, int color) {
        IMAGE.setPixelRGBA(x + X_OFFSET, y + Y_OFFSET, color);
    }

    /**
     * Gets the width of the image.
     * @return  the width of the image
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Gets the height of the image.
     * @return  the height of the image
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public Set<Pair<Integer, Integer>> getInterpolatablePoints() {
        return INTERPOLATABLE_POINTS;
    }

    /**
     * Gets the wrapped image.
     * @return  the wrapped image
     */
    public NativeImage getImage() {
        return IMAGE;
    }

}
