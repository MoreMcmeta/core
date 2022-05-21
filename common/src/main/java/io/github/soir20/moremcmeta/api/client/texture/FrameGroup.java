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

public interface FrameGroup<F extends FrameView> {

    F frame(int index);

    int frames();

    /**
     * Indicates that an illegal frame index was accessed.
     * @author soir20
     */
    class FrameGroupIndexOutOfBoundsException extends IndexOutOfBoundsException {

        /**
         * Creates an exception to indicate that a frame with a certain index does not
         * exist.
         * @param index     the illegal index accessed
         */
        public FrameGroupIndexOutOfBoundsException(int index) {
            super("Frame index out of range: " + index);
        }
    }

}
