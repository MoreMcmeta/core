package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link NativeImage}.
 * @author soir20
 */
public class NativeImageFrame {
    private final NativeImage[] MIPMAPS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int FRAME_TIME;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final boolean AUTO_CLOSE;

    /**
     * Creates a new frame based on frame data.
     * @param frameData     general data associated with the frame
     * @param mipmaps       mipmapped images for this frame (starting with the original image)
     * @param blur          whether to blur the frame when it is uploaded
     * @param clamp         whether to clamp the frame when it is uploaded
     * @param autoClose     whether to close the image automatically when it is uploaded
     */
    public NativeImageFrame(FrameReader.FrameData frameData, NativeImage[] mipmaps,
                            boolean blur, boolean clamp, boolean autoClose) {
        requireNonNull(frameData, "Frame data cannot be null");
        MIPMAPS = requireNonNull(mipmaps, "Mipmaps cannot be null");
        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        FRAME_TIME = frameData.getTime();
        BLUR = blur;
        CLAMP = clamp;
        AUTO_CLOSE = autoClose;
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
        for (int level = 0; level < MIPMAPS.length; level++) {
            int mipmappedX = point.getX() >> level;
            int mipmappedY = point.getY() >> level;
            int mipmappedSkipX = X_OFFSET >> level;
            int mipmappedSkipY = Y_OFFSET >> level;
            int mipmappedWidth = WIDTH >> level;
            int mipmappedHeight = HEIGHT >> level;

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                MIPMAPS[level].upload(level, mipmappedX, mipmappedY, mipmappedSkipX, mipmappedSkipY,
                        mipmappedWidth, mipmappedHeight, BLUR, CLAMP, MIPMAPS.length > 1, AUTO_CLOSE);
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
        return MIPMAPS.length;
    }

    /**
     * Gets the image associated with this frame at a certain mipmap level.
     * @param level     the mipmap level
     * @return  the image at the given mipmap level
     */
    public NativeImage getImage(int level) {
        return MIPMAPS[level];
    }

}
