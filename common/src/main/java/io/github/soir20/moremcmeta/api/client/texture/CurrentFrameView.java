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

import io.github.soir20.moremcmeta.api.math.Point;

/**
 * {@link FrameView} with the ability to adjust the current frame. Predefined frames are
 * immutable, but a frame can be generated from a predefined frame or an already-generated
 * frame.
 * @author soir20
 * @since 4.0
 */
public interface CurrentFrameView extends FrameView {

    /**
     * Generates a frame from the current frame by applying the given {@link ColorTransform} over
     * the given area. The current frame may be a predefined frame or a generated frame. Predefined
     * frames are never modified.
     * @param transform     the transformation to apply to the given points
     * @param applyArea     the points to apply the transformation to
     * @throws io.github.soir20.moremcmeta.api.client.texture.FrameView.PixelOutOfBoundsException if a pixel
     *         in the `applyArea` is out of the frame's bounds
     * @throws IllegalFrameReference if this view is no longer valid
     */
    void generateWith(ColorTransform transform, Iterable<Point> applyArea);

    /**
     * Makes the predefined frame with the given index the current frame. The next call to
     * {@link #generateWith(ColorTransform, Iterable)} by any plugin will generate a frame based
     * on this predefined frame, unless another predefined frame is made the current frame.
     * @param index     the index of the predefined frame to make current
     * @throws io.github.soir20.moremcmeta.api.client.texture.FrameView.FrameIndexOutOfBoundsException if the
     *         provided index is outside the range of legal frame indices
     * @throws IllegalFrameReference if this view is no longer valid
     */
    void replaceWith(int index);

}
