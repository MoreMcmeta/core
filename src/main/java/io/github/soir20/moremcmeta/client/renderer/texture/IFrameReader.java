package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.resources.data.AnimationMetadataSection;

import java.util.List;

/**
 * Creates animated (tickable) textures.
 * @param <F>   tickable texture type
 * @author soir20
 */
public interface IFrameReader<F extends IAnimationFrame<? extends IUploadableMipmap>> {
    int EMPTY_TIME = -1;

    /**
     * Creates frames based on file data.
     * @param imageWidth        the width of the source image
     * @param imageHeight       the height of the source image
     * @param metadata          image animation metadata
     * @return frames that can be used in an animated texture
     */
    List<F> read(int imageWidth, int imageHeight, AnimationMetadataSection metadata);

    class FrameData {
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
