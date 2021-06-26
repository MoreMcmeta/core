package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link IRGBAImage}.
 * @author soir20
 */
public class RGBAImageFrame {
    private final List<? extends IRGBAImage> MIPMAPS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int FRAME_TIME;

    /**
     * Creates a new frame based on frame data.
     * @param frameData     general data associated with the frame
     * @param mipmaps       mipmapped images for this frame (starting with the original image)
     */
    public RGBAImageFrame(FrameReader.FrameData frameData, List<? extends IRGBAImage> mipmaps) {
        requireNonNull(frameData, "Frame data cannot be null");
        MIPMAPS = requireNonNull(mipmaps, "Mipmaps cannot be null");
        if (mipmaps.size() == 0) {
            throw new IllegalArgumentException("At least one mipmap must be provided");
        }

        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        FRAME_TIME = frameData.getTime();
    }

    /**
     * Gets the length (time) of this frame.
     * @return  the length of this frame in ticks
     */
    public int getFrameTime() {
        return FRAME_TIME;
    }

    /**
     * Uploads this frame at a given position in the active texture.
     * @param point     point to upload the top-left corner of this frame at
     */
    public void uploadAt(Point point) {
        requireNonNull(point, "Point cannot be null");

        if (point.getX() < 0 || point.getY() < 0) {
            throw new IllegalArgumentException("Point coordinates must be greater than zero");
        }

        for (int level = 0; level < MIPMAPS.size(); level++) {
            int mipmappedX = point.getX() >> level;
            int mipmappedY = point.getY() >> level;

            IRGBAImage mipmap = MIPMAPS.get(level);
            int mipmappedWidth = mipmap.getWidth();
            int mipmappedHeight = mipmap.getHeight();

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                mipmap.upload(mipmappedX, mipmappedY);
            }
        }
    }

    /**
     * Gets the width of this frame in pixels.
     * @return  the width of this frame in pixels
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Gets the height of this frame in pixels.
     * @return  the height of this frame in pixels
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Gets the x-offset of the top-left corner of this frame in pixels.
     * @return  the x-offset of this frame in pixels
     */
    public int getXOffset() {
        return X_OFFSET;
    }

    /**
     * Gets the y-offset of the top-left corner of this frame in pixels.
     * @return  the y-offset of this frame in pixels
     */
    public int getYOffset() {
        return Y_OFFSET;
    }

    /**
     * Gets the mipmap level of this frame.
     * @return the mipmap level of this frame
     */
    public int getMipmapLevel() {
        return MIPMAPS.size() - 1;
    }

    /**
     * Gets the image associated with this frame at a certain mipmap level.
     * @param level     the mipmap level
     * @return  the image at the given mipmap level
     */
    public IRGBAImage getImage(int level) {
        if (level >= MIPMAPS.size()) {
            throw new IllegalArgumentException("There is no mipmap of level " + level);
        }

        return MIPMAPS.get(level);
    }

}
