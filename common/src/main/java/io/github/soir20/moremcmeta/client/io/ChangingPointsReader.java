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
     * Gets the pixels that will change for every mipmap of an image.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame
     * @param frameHeight   the height of a frame
     * @param mipmap        number of mipmap levels to use
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    public List<IRGBAImage.VisibleArea> read(IRGBAImage image, int frameWidth, int frameHeight, int mipmap) {
        requireNonNull(image, "Image cannot be null");
        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();

        // Find points in original image
        IRGBAImage.VisibleArea.Builder noMipmapBuilder = new IRGBAImage.VisibleArea.Builder();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int frameX = x % frameWidth;
                int frameY = y % frameHeight;

                // We want to detect a point that changes in any frame
                if (image.getPixel(x, y) != image.getPixel(frameX, frameY)) {
                    noMipmapBuilder.addPixel(frameX, frameY);
                }

            }
        }
        visibleAreas.add(noMipmapBuilder.build());

        // Point coordinates will be different for all mipmap levels
        for (int level = 1; level <= mipmap; level++) {
            IRGBAImage.VisibleArea.Builder mipmapBuilder = new IRGBAImage.VisibleArea.Builder();

            for (Point point : visibleAreas.get(0)) {
                mipmapBuilder.addPixel(point.getX() >> level, point.getY() >> level);
            }

            visibleAreas.add(mipmapBuilder.build());
        }

        return visibleAreas;
    }

}
