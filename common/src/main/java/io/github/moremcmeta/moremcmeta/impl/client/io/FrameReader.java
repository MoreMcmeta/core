/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2023 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.client.io;

import com.google.common.collect.ImmutableList;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Creates all the frames in a multi-frame texture. It is reusable for all images with the given type of frame.
 * @param <F>   tickable texture type
 * @author soir20
 */
public final class FrameReader<F> {
    private final Function<FrameData, ? extends F> FRAME_FACTORY;

    /**
     * Creates a new reader.
     * @param frameFactory      creates frames based on frame data. Cannot return null.
     */
    public FrameReader(Function<FrameData, ? extends F> frameFactory) {
        FRAME_FACTORY = requireNonNull(frameFactory, "Frame factory cannot be null");
    }

    /**
     * Creates frames based on file data.
     * @param imageWidth        the width of the source image
     * @param imageHeight       the height of the source image
     * @param frameWidth        the width of a frame in the source image
     * @param frameHeight       the height of a frame in the source image
     * @return frames that can be used in a multi-frame texture
     */
    public ImmutableList<F> read(int imageWidth, int imageHeight, int frameWidth, int frameHeight) {

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
        }

        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Frame width and height must not be zero or negative");
        }
        int numFramesX = imageWidth / frameWidth;
        int numFramesY = imageHeight / frameHeight;

        return findFrames(frameWidth, frameHeight, numFramesX, numFramesY);
    }

    /**
     * Gets frames when they are not defined in metadata.
     * @param frameWidth    width of a single frame (pixels)
     * @param frameHeight   height of a single frame (pixels)
     * @param numFramesX    number of frames per row
     * @param numFramesY    number of frames per column
     * @return  the list of discovered frames
     */
    private ImmutableList<F> findFrames(int frameWidth, int frameHeight, int numFramesX, int numFramesY) {
        ImmutableList.Builder<F> frames = ImmutableList.builder();

        for (int row = 0; row < numFramesY; row++) {
            for (int column = 0; column < numFramesX; column++) {
                FrameData data = new FrameData(frameWidth, frameHeight,
                        column * frameWidth, row * frameHeight);

                F nextFrame = FRAME_FACTORY.apply(data);
                requireNonNull(nextFrame, "Found frame was created as null");

                frames.add(nextFrame);
            }
        }

        return frames.build();
    }

    /**
     * Holds data about a single frame.
     * @author soir20
     */
    public static final class FrameData {
        private final int WIDTH;
        private final int HEIGHT;
        private final int X_OFFSET;
        private final int Y_OFFSET;

        /**
         * Creates a new holder for frame data.
         * @param width     width of the frame (pixels)
         * @param height    height of the frame (pixels)
         * @param xOffset   x-offset of the frame (pixels)
         * @param yOffset   y-offset of the frame (pixels)
         */
        public FrameData(int width, int height, int xOffset, int yOffset) {
            WIDTH = width;
            HEIGHT = height;
            X_OFFSET = xOffset;
            Y_OFFSET = yOffset;
        }

        /**
         * Gets the width of this frame.
         * @return  the width of the frame in pixels
         */
        public int width() {
            return WIDTH;
        }

        /**
         * Gets the height of this frame.
         * @return  the height of the frame in pixels
         */
        public int height() {
            return HEIGHT;
        }

        /**
         * Gets the x-offset of this frame.
         * @return  the x-offset of this frame in pixels
         */
        public int xOffset() {
            return X_OFFSET;
        }

        /**
         * Gets the y-offset of this frame.
         * @return  the y-offset of this frame in pixels
         */
        public int yOffset() {
            return Y_OFFSET;
        }

    }

}
