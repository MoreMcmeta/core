package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.function.BiFunction;

/**
 * Generates interpolated images in between two other images.
 * Color format: AAAA AAAA RRRR RRRR GGGG GGGG BBBB BBBB in binary, stored as an integer (32 bits total)
 * @param <T>   type of image to mix
 * @author soir20
 */
public class RGBAInterpolator<T extends IRGBAImage> implements IInterpolator<T> {
    private final BiFunction<Integer, Integer, T> IMAGE_GETTER;

    /**
     * Creates a new interpolator.
     * @param imageGetter      accepts a width and height to produce interpolated images.
     *                         Colors are overwritten. The longest width and the longest height
     *                         are selected between the start and end images.
     */
    public RGBAInterpolator(BiFunction<Integer, Integer, T> imageGetter) {
        IMAGE_GETTER = imageGetter;
    }

    /**
     * Generates an interpolate images between two images.
     * @param steps     the number of steps it should take from the start image to reach the end image
     * @param step      the current step in the interpolation. Start at 1, and end at steps - 1.
     * @param start     the image to start interpolation at (not returned)
     * @param end       the image to end interpolation at (not returned)
     * @return  the interpolated frame at this step
     */
    public T interpolate(int steps, int step, T start, T end) {
        if (step < 1 || step >= steps) {
            throw new IllegalArgumentException("Step must be between 1 and steps - 1 (inclusive)");
        }

        double ratio = 1.0 - (step / (double) steps);
        return mixImage(ratio, start, end);
    }

    /**
     * Mixes the colors in two images to produce a new image.
     * @param startProportion   proportion of start color to mix (1 - proportion of end color)
     * @param start             start image (unchanged)
     * @param end               end image (unchanged)
     * @return  the image with mixed colors (using the largest dimension in each direction)
     */
    private T mixImage(double startProportion, T start, T end) {
        int maxWidth = Math.max(start.getWidth(), end.getWidth());
        int maxHeight = Math.max(start.getHeight(), end.getHeight());
        T output = IMAGE_GETTER.apply(maxWidth, maxHeight);

        for (int yPos = 0; yPos < maxHeight; yPos++) {
            for (int xPos = 0; xPos < maxWidth; xPos++) {
                int startColor= getPixel(start, xPos, yPos);
                int endColor = getPixel(end, xPos, yPos);
                int mixedColor = mixPixel(startProportion, startColor, endColor);

                output.setPixel(xPos, yPos, mixedColor);
            }
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
    private int getPixel(T image, int x, int y) {
        return x > image.getWidth() || y > image.getHeight() ? 0 : image.getPixel(x, y);
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
                extractComponent(startColor, Component.RED), extractComponent(endColor, Component.RED));
        int green = mixComponent(startProportion,
                extractComponent(startColor, Component.GREEN), extractComponent(endColor, Component.GREEN));
        int blue = mixComponent(startProportion,
                extractComponent(startColor, Component.BLUE), extractComponent(endColor, Component.BLUE));

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
     * Extracts the value of one component from an RGB color.
     * @param color         the RGB color to extract from
     * @param component     the component to extract
     * @return  the value of the component in this color
     */
    private int extractComponent(int color, Component component) {
        switch (component) {
            case RED:
                return color >> 16 & 255;
            case GREEN:
                return color >> 8 & 255;
            case BLUE:
                return color & 255;
        }

        throw new IllegalArgumentException("Must specify a valid RGB component");
    }

    /**
     * Represents a red, blue, or green component in an RGB color.
     * @author soir20
     */
    private enum Component {
        RED,
        GREEN,
        BLUE
    }

}
