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

package io.github.soir20.moremcmeta.api.client.texture;

import java.util.Optional;

public interface FrameView {

    int color(int x, int y);

    int width();

    int height();

    Optional<Integer> index();

    int predefinedFrames();

    /**
     * Indicates that a point outside a frame's bounds was accessed.
     * @author soir20
     */
    class PixelOutOfBoundsException extends RuntimeException {

        /**
         * Creates a new exception to indicate that a point outside a frame's bounds was accessed.
         * @param x     x coordinate of the point accessed
         * @param y     y coordinate of the point accessed
         */
        public PixelOutOfBoundsException(int x, int y) {
            super("Point (" + x + ", " + y + ") is outside the frame");
        }
    }

    /**
     * Indicates that a {@link FrameView} was used after it became invalid.
     * @author soir20
     */
    class IllegalFrameReference extends IllegalStateException {

        /**
         * Creates a new exception to indicate that a reference to a frame view is invalid.
         */
        public IllegalFrameReference() {
            super("Cannot use frame view beyond intended point");
        }
    }

    /**
     * Indicates that an illegal predefined frame index was accessed.
     * @author soir20
     */
    class FrameIndexOutOfBoundsException extends IndexOutOfBoundsException {

        /**
         * Creates an exception to indicate that a frame with a certain index does not
         * exist.
         * @param index     the illegal index accessed
         */
        public FrameIndexOutOfBoundsException(int index) {
            super("Frame index out of range: " + index);
        }
    }

}
