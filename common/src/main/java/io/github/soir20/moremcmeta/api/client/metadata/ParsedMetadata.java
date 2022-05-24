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

package io.github.soir20.moremcmeta.api.client.metadata;

import io.github.soir20.moremcmeta.api.math.NegativeDimensionException;

import java.util.Optional;

/**
 * Contains data parsed by the {@link MetadataParser}.
 * @author soir20
 * @since 4.0
 */
public interface ParsedMetadata {

    /**
     * Gets the frame size read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * frame sizes; otherwise, the metadata is invalid.
     * @return the frame size this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<FrameSize> frameSize() {
        return Optional.empty();
    }

    /**
     * Gets the blur boolean read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * booleans; otherwise, the metadata is invalid.
     * @return the blur setting this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Boolean> blur() {
        return Optional.empty();
    }

    /**
     * Gets the clamp boolean read from the metadata. {@link Optional#empty()} indicates "no opinion."
     * All plugins that do not return {@link Optional#empty()} from this method must return equal
     * booleans; otherwise, the metadata is invalid.
     * @return the clamp setting this plugin determined or {@link Optional#empty()} if this plugin
     *         has no opinion.
     */
    default Optional<Boolean> clamp() {
        return Optional.empty();
    }

    /**
     * Returns a string explaining why the metadata is invalid for some reason. The texture associated
     * with the metadata will be skipped if this method does not return {@link Optional#empty()}.
     * @return the reason the metadata is invalid or {@link Optional#empty()} if the metadata is valid
     */
    default Optional<String> invalidReason() {
        return Optional.empty();
    }

    /**
     * Holds the frame width and height as a single object.
     * @author soir20
     * @since 4.0
     */
    final class FrameSize {
        private final int WIDTH;
        private final int HEIGHT;

        /**
         * Creates a new object representing a frame size.
         * @param width     width of a frame
         * @param height    height of a frame
         * @throws NegativeDimensionException if the width or the height is negative
         */
        public FrameSize(int width, int height) {
            if (width < 0) {
                throw new NegativeDimensionException(width);
            }

            if (height < 0) {
                throw new NegativeDimensionException(height);
            }

            WIDTH = width;
            HEIGHT = height;
        }

        /**
         * Gets the width of a frame.
         * @return the width of a frame
         */
        public int width() {
            return WIDTH;
        }

        /**
         * Gets the height of a frame.
         * @return the height of a frame
         */
        public int height() {
            return HEIGHT;
        }

        /**
         * Checks if another object is equivalent to this frame size.
         * @param other     the other object to compare this frame size to
         * @return whether the other object represents an equivalent frame size
         */
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FrameSize otherSize)) {
                return false;
            }

            return width() == otherSize.width() && height() == otherSize.height();
        }

        /**
         * Gets a hash code for this frame size.
         * @return a hash code for this frame size
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return 31 * WIDTH + HEIGHT;
        }

    }
}
