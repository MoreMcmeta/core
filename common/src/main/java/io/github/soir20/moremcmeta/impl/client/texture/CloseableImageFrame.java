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

package io.github.soir20.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.io.FrameReader;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link CloseableImage}.
 * @author soir20
 */
public class CloseableImageFrame {
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final SharedMipmapLevel SHARED_MIPMAP_LEVEL;
    private ImmutableList<? extends CloseableImage> mipmaps;

    /**
     * Creates a new frame based on frame data.
     * @param frameData             general data associated with the frame
     * @param mipmaps               mipmapped images for this frame (starting with the original image)
     * @param sharedMipmapLevel     mipmap level potentially shared with other frames. Upon the mipmap level being
     *                              lowered, this frame's mipmap level lowers, and its extra mipmaps close.
     */
    public CloseableImageFrame(FrameReader.FrameData frameData, ImmutableList<? extends CloseableImage> mipmaps,
                               SharedMipmapLevel sharedMipmapLevel) {
        requireNonNull(frameData, "Frame data cannot be null");
        this.mipmaps = requireNonNull(mipmaps, "Mipmaps cannot be null");
        if (mipmaps.isEmpty()) {
            throw new IllegalArgumentException("At least one mipmap must be provided");
        }

        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        SHARED_MIPMAP_LEVEL = requireNonNull(sharedMipmapLevel, "Shared mipmap level cannot be null");
        SHARED_MIPMAP_LEVEL.addSubscriber(this);
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

        for (int level = 0; level < mipmaps.size(); level++) {
            int mipmappedX = point.getX() >> level;
            int mipmappedY = point.getY() >> level;

            CloseableImage mipmap = mipmaps.get(level);
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
        return mipmaps.size() - 1;
    }

    /**
     * Lowers the mipmap level of this frame, closing all mipmaps above the provided level.
     * @param newMipmapLevel        the maximum new mipmap level
     */
    public void lowerMipmapLevel(int newMipmapLevel) {
        if (newMipmapLevel == getMipmapLevel()) {
            return;
        }

        if (newMipmapLevel < 0) {
            throw new IllegalArgumentException("New mipmap level must be at least zero");
        }

        if (newMipmapLevel > getMipmapLevel()) {
            throw new IllegalArgumentException("New mipmap level " + newMipmapLevel + " is greater than current " +
                    "mipmap level " + getMipmapLevel());
        }

        for (int level = newMipmapLevel + 1; level < mipmaps.size(); level++) {
            mipmaps.get(level).close();
        }

        mipmaps = mipmaps.subList(0, newMipmapLevel + 1);
        SHARED_MIPMAP_LEVEL.lowerMipmapLevel(newMipmapLevel);
    }

    /**
     * Gets the image associated with this frame at a certain mipmap level.
     * @param level     the mipmap level
     * @return  the image at the given mipmap level
     */
    public CloseableImage getImage(int level) {
        if (level >= mipmaps.size()) {
            throw new IllegalArgumentException("There is no mipmap of level " + level);
        }

        return mipmaps.get(level);
    }

    /**
     * Represents a shared open/close status for multiple {@link CloseableImageFrame}s referencing the same mipmaps or
     * that should have the same mipmap level. All subscribers run when the mipmap level is lowered.
     * @author soir20
     */
    public static final class SharedMipmapLevel {
        private final List<CloseableImageFrame> SUBSCRIBERS;
        private int mipmapLevel;

        /**
         * Creates a new shared mipmap level.
         * @param currentLevel      current mipmap level shared between all frames
         */
        public SharedMipmapLevel(int currentLevel) {
            if (currentLevel < 0) {
                throw new IllegalArgumentException("Mipmap level cannot be negative");
            }

            SUBSCRIBERS = new ArrayList<>();
            mipmapLevel = currentLevel;
        }

        /**
         * Adds an {@link CloseableImageFrame} whose mipmap level will be lowered when the shared level is lowered. This
         * frame's mipmap level will be lowered to the current shared level immediately.
         */
        public void addSubscriber(CloseableImageFrame frame) {
            requireNonNull(frame, "Frame cannot be null");
            frame.lowerMipmapLevel(mipmapLevel);
            SUBSCRIBERS.add(frame);
        }

        /**
         * Lowers the shared mipmap level and runs all subscribers. It is not guaranteed subscribers will be run
         * if the new mipmap level is the same as the current level.
         * @param newMipmapLevel      new mipmap level of this shared level. Must be lower than or the same as the
         *                            current level.
         */
        public void lowerMipmapLevel(int newMipmapLevel) {
            if (newMipmapLevel == mipmapLevel) {
                return;
            }

            if (newMipmapLevel > mipmapLevel) {
                throw new IllegalArgumentException("New mipmap level cannot be greater than old one");
            }

            if (newMipmapLevel < 0) {
                throw new IllegalArgumentException("New mipmap level cannot be negative");
            }

            mipmapLevel = newMipmapLevel;
            SUBSCRIBERS.forEach((subscriber) -> subscriber.lowerMipmapLevel(newMipmapLevel));
        }

    }

}
