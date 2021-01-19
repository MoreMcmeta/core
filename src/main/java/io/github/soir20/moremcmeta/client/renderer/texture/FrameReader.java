package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.data.AnimationMetadataSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Creates animated (tickable) textures.
 * @param <F>   tickable texture type
 * @author soir20
 */
public class FrameReader<F extends IAnimationFrame<? extends IUploadableMipmap>> {
    private final Function<FrameData, F> FRAME_FACTORY;

    public FrameReader(Function<FrameData, F> frameFactory) {
        FRAME_FACTORY = frameFactory;
    }

    /**
     * Creates frames based on file data.
     * @param imageWidth        the width of the source image
     * @param imageHeight       the height of the source image
     * @param metadata          image animation metadata
     * @return frames that can be used in an animated texture
     */
    public List<F> read(int imageWidth, int imageHeight, AnimationMetadataSection metadata) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
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

    private Pair<Integer, Integer> frameIndexToPosition(int index, int numFramesX) {
        int xPos = index % numFramesX;
        int yPos = index / numFramesX;
        return new Pair<>(xPos, yPos);
    }

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

    public static class FrameData {
        public static final int EMPTY_TIME = -1;

        private final int WIDTH;
        private final int HEIGHT;
        private final int X_OFFSET;
        private final int Y_OFFSET;
        private final int TIME;

        public FrameData(int width, int height, int xOffset, int yOffset, int time) {
            WIDTH = width;
            HEIGHT = height;
            X_OFFSET = xOffset;
            Y_OFFSET = yOffset;
            TIME = time;
        }

        public int getWidth() {
            return WIDTH;
        }

        public int getHeight() {
            return HEIGHT;
        }

        public int getXOffset() {
            return X_OFFSET;
        }

        public int getYOffset() {
            return Y_OFFSET;
        }

        public int getTime() {
            return TIME;
        }

    }

}
