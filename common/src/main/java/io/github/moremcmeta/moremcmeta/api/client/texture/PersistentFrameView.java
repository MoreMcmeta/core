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
 * A frame view that never becomes invalid and never throws an {@link FrameView.IllegalFrameReference}.
 * @author soir20
 * @since 4.0.0
 */
public interface PersistentFrameView extends FrameView {

    /**
     * Gets the color at a specific pixel in this frame.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return the color of the pixel
     * @throws FrameView.PixelOutOfBoundsException if the requested pixel is outside
     *                                             the frame's bounds
     */
    int color(int x, int y);

}
