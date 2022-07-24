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

/**
 * A {@link FrameView} that represents a frame that can be uploaded.
 * @author soir20
 * @since 4.0.0
 */
public interface UploadableFrameView extends FrameView {

    /**
     * Uploads the current frame to the texture currently bound in OpenGL.
     * @param x     x-coordinate of the point to upload to
     * @param y     y-coordinate of the point to upload to
     * @throws IllegalFrameReference if this view is no longer valid
     * @throws NegativeUploadPointException if the provided upload point is negative. The upload point may
     *                                      still be positive and out of bounds even if no exception is
     *                                      thrown.
     */
    void upload(int x, int y);

    /**
     * Indicates that a point outside a frame's bounds was accessed.
     * @author soir20
     * @since 4.0.0
     */
    final class NegativeUploadPointException extends RuntimeException {

        /**
         * Creates a new exception to indicate that a point outside a frame's bounds was accessed.
         * @param x     x-coordinate of the point accessed
         * @param y     y-coordinate of the point accessed
         */
        public NegativeUploadPointException(int x, int y) {
            super("Point (" + x + ", " + y + ") is negative and thus invalid");
        }
    }

}
