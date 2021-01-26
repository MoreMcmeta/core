package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Creates all the frames in an animated texture.
 * @param <F>   tickable texture type
 * @author soir20
 */
public class FrameReader<F extends IAnimationFrame> {
    private final Function<FrameData, F> FRAME_FACTORY;

    /**
     * Creates a new reader, which is reusable for all images with the given type of frame.
     * @param frameFactory      creates frames based on frame data
     */
    public FrameReader(Function<FrameData, F> frameFactory) {
        FRAME_FACTORY = frameFactory;
    }

    /**
     * Creates frames based on file data.
     * @param imageWidth        the width of the source image
     * @param imageHeight       the height of the source image
     * @param metadata          image animation info
     * @return frames that can be used in an animated texture
     */
    public List<F> read(int imageWidth, int imageHeight, AnimationMetadataSection metadata) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
        }

        if (metadata.getFrameWidth(-1) == 0 || metadata.getFrameHeight(-1) == 0) {
            throw new IllegalArgumentException("Frame width and height must not be empty");
        }

        Pair<Integer, Integer> frameSize = metadata.getSpriteSize(imageWidth, imageHeight);
        int frameWidth = frameSize.getFirst();
        int frameHeight = frameSize.getSecond();

        int numFramesX = imageWidth / frameWidth;
        int numFramesY = imageHeight / frameHeight;

        if (metadata.getFrameCount() > 0) {
            return getPredefinedFrames(metadata, frameWidth, frameHeight, numFramesX);
        } else {
            return findFrames(frameWidth, frameHeight, numFramesX, numFramesY);
        }
    }

    /**
     * Converts a frame's index to its position in an image.
     * @param index         index of a frame
     * @param numFramesX    number of frames per row
     * @return  a coordinate pair representing the frame's position: (frame widths, frame heights)
     */
    private Pair<Integer, Integer> frameIndexToPosition(int index, int numFramesX) {
        int xPos = index % numFramesX;
        int yPos = index / numFramesX;
        return new Pair<>(xPos, yPos);
    }

    /**
     * Gets frames when they are already defined in metadata.
     * @param metadata      image animation info
     * @param frameWidth    width of a single frame
     * @param frameHeight   height of a single frame
     * @param numFramesX    number of frames per row
     * @return  the list of predefined frames
     */
    private List<F> getPredefinedFrames(AnimationMetadataSection metadata, int frameWidth, int frameHeight,
                                        int numFramesX) {
        List<F> frames = new ArrayList<>();

        for (int frame = 0; frame < metadata.getFrameCount(); frame++) {
            int index = metadata.getFrameIndex(frame);
            int time = metadata.getFrameTimeSingle(frame);

            Pair<Integer, Integer> framePos = frameIndexToPosition(index, numFramesX);
            int xOffset = framePos.getFirst() * frameWidth;
            int yOffset = framePos.getSecond() * frameHeight;

            FrameData data = new FrameData(frameWidth, frameHeight, xOffset, yOffset, time);
            frames.add(FRAME_FACTORY.apply(data));
        }

        return frames;
    }

    /**
     * Gets frames when they are not defined in metadata.
     * @param frameWidth    width of a single frame (pixels)
     * @param frameHeight   height of a single frame (pixels)
     * @param numFramesX    number of frames per row
     * @param numFramesY    number of frames per column
     * @return  the list of discovered frames
     */
    private List<F> findFrames(int frameWidth, int frameHeight, int numFramesX, int numFramesY) {
        List<F> frames = new ArrayList<>();

        for (int row = 0; row < numFramesY; row++) {
            for (int column = 0; column < numFramesX; column++) {
                FrameData data = new FrameData(frameWidth, frameHeight,
                        column * frameWidth, row * frameHeight, FrameData.EMPTY_TIME);
                frames.add(FRAME_FACTORY.apply(data));
            }
        }

        return frames;
    }

    /**
     * Encapsulates data about a single frame.
     * @author soir20
     */
    public static class FrameData {
        public static final int EMPTY_TIME = -1;

        private final int WIDTH;
        private final int HEIGHT;
        private final int X_OFFSET;
        private final int Y_OFFSET;
        private final int TIME;

        /**
         * Creates a new holder for frame data.
         * @param width     width of the frame (pixels)
         * @param height    height of the frame (pixels)
         * @param xOffset   x-offset of the frame (pixels)
         * @param yOffset   y-offset of the frame (pixels)
         * @param time      length of the frame (ticks)
         */
        public FrameData(int width, int height, int xOffset, int yOffset, int time) {
            WIDTH = width;
            HEIGHT = height;
            X_OFFSET = xOffset;
            Y_OFFSET = yOffset;
            TIME = time;
        }

        /**
         * Gets the width of this frame.
         * @return  the width of the frame in pixels
         */
        public int getWidth() {
            return WIDTH;
        }

        /**
         * Gets the height of this frame.
         * @return  the height of the frame in pixels
         */
        public int getHeight() {
            return HEIGHT;
        }

        /**
         * Gets the x-offset of this frame.
         * @return  the x-offset of this frame in pixels
         */
        public int getXOffset() {
            return X_OFFSET;
        }

        /**
         * Gets the y-offset of this frame.
         * @return  the y-offset of this frame in pixels
         */
        public int getYOffset() {
            return Y_OFFSET;
        }

        /**
         * Gets the length (time) of this frame.
         * @return  the length of this frame (in ticks)
         */
        public int getTime() {
            return TIME;
        }

    }

}
