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

package io.github.moremcmeta.moremcmeta.api.client.texture;

/**
 * <p>A view of information about a texture frame without any underlying implementation detail. This interface
 * specifies an immutable view of a frame, but sub-interfaces may specify additional methods that make the
 * view mutable. A view may have a limited lifetime, after which {@link IllegalFrameReferenceException} exceptions
 * are thrown when the view's methods are called, specified by its implementation.</p>
 *
 * <p>Note about coordinate points: The top-left corner of the frame is considered to be at (0, 0), and
 * only positive coordinate values are within bounds. That is, the bottom right corner of the frame is at
 * (width - 1, height - 1).</p>
 * @author soir20
 * @since 4.0.0
 */
public interface FrameView {

    /**
     * Gets the width of the frame.
     * @return width of the frame
     * @throws IllegalFrameReferenceException if this view is no longer valid
     */
    int width();

    /**
     * Gets the height of the frame.
     * @return height of the frame
     * @throws IllegalFrameReferenceException if this view is no longer valid
     */
    int height();

}
