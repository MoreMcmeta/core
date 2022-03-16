/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.impl.client.io;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Creates all the frames in an animated texture. It is is reusable for all images with the given type of frame.
 * @param <F>   tickable texture type
 * @author soir20
 */
public class FrameReader<F> {
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
     * @param metadata          image animation info
     * @return frames that can be used in an animated texture
     */
    public ImmutableList<F> read(int imageWidth, int imageHeight, AnimationMetadataSection metadata) {
        requireNonNull(metadata, "Animation metadata cannot be null");

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("Image must not be empty");
        }

        if (metadata.getFrameWidth(-1) == 0 || metadata.getFrameHeight(-1) == 0) {
            throw new IllegalArgumentException("Frame width and height must not be empty");
        }

        Pair<Integer, Integer> frameSize = metadata.getFrameSize(imageWidth, imageHeight);
        int frameWidth = frameSize.getFirst();
        int frameHeight = frameSize.getSecond();

        int numFramesX = imageWidth / frameWidth;
        int numFramesY = imageHeight / frameHeight;

        AtomicBoolean hasFrames = new AtomicBoolean(false);
        metadata.forEachFrame((index, time) -> hasFrames.set(true));

        if (hasFrames.get()) {
            return getPredefinedFrames(metadata, frameWidth, frameHeight, numFramesX, numFramesY);
        } else {
            return findFrames(metadata, frameWidth, frameHeight, numFramesX, numFramesY);
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
     * @param numFramesY    number of frames per column
     * @return  the list of predefined frames
     */
    private ImmutableList<F> getPredefinedFrames(AnimationMetadataSection metadata, int frameWidth,
                                                 int frameHeight, int numFramesX, int numFramesY) {
        ImmutableList.Builder<F> frames = ImmutableList.builder();

        // Cache frames so we can reuse them if they repeat
        Map<Integer, F> createdFramesByIndex = new HashMap<>();

        metadata.forEachFrame((index, time) -> {

            // Use cached frames
            if (createdFramesByIndex.containsKey(index)) {
                frames.add(createdFramesByIndex.get(index));
                return;
            }

            // If not already created, generate new frame
            if (index >= numFramesX * numFramesY) {
                throw new IllegalArgumentException("Index " + index + " would put frame out of image bounds");
            }

            Pair<Integer, Integer> framePos = frameIndexToPosition(index, numFramesX);
            int xOffset = framePos.getFirst() * frameWidth;
            int yOffset = framePos.getSecond() * frameHeight;

            FrameData data = new FrameData(frameWidth, frameHeight, xOffset, yOffset, time);

            F nextFrame = FRAME_FACTORY.apply(data);
            requireNonNull(nextFrame, "Predetermined frame was created as null");

            frames.add(nextFrame);
            createdFramesByIndex.put(index, nextFrame);
        });

        return frames.build();
    }

    /**
     * Gets frames when they are not defined in metadata.
     * @param metadata      image animation info
     * @param frameWidth    width of a single frame (pixels)
     * @param frameHeight   height of a single frame (pixels)
     * @param numFramesX    number of frames per row
     * @param numFramesY    number of frames per column
     * @return  the list of discovered frames
     */
    private ImmutableList<F> findFrames(AnimationMetadataSection metadata, int frameWidth, int frameHeight,
                               int numFramesX, int numFramesY) {
        ImmutableList.Builder<F> frames = ImmutableList.builder();

        for (int row = 0; row < numFramesY; row++) {
            for (int column = 0; column < numFramesX; column++) {
                FrameData data = new FrameData(frameWidth, frameHeight,
                        column * frameWidth, row * frameHeight, metadata.getDefaultFrameTime());

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
