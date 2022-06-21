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

import io.github.soir20.moremcmeta.api.client.texture.Color;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import io.github.soir20.moremcmeta.api.client.texture.PersistentFrameView;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A persistent view of a predefined frame that never becomes invalid. This class assumes that the
 * frame provided is not changed externally once this object is created.
 * @author soir20
 */
public class PredefinedFrameView implements PersistentFrameView {
    private final CloseableImageFrame FRAME;
    private final int INDEX;

    /**
     * Creates a new predefined frame view.
     * @param frame                 the predefined frame
     * @param index                 the index of the predefined frame among all predefined frames
     */
    public PredefinedFrameView(CloseableImageFrame frame, int index) {
        FRAME = requireNonNull(frame, "Frame cannot be null");
        INDEX = index;
    }

    /**
     * Gets the width of this frame.
     * @return the width of this frame
     */
    @Override
    public int width() {
        return FRAME.width();
    }

    /**
     * Gets the height of this frame.
     * @return the height of this frame
     */
    @Override
    public int height() {
        return FRAME.height();
    }

    /**
     * Gets the index of this frame among all predefined frames (always present).
     * @return the index of this frame
     */
    @Override
    public Optional<Integer> index() {
        return Optional.of(INDEX);
    }

    /**
     * Gets the color at a specific pixel in this frame.
     * @param x     x-coordinate of the pixel
     * @param y     y-coordinate of the pixel
     * @return the color of the pixel
     * @throws FrameView.PixelOutOfBoundsException if the requested pixel is outside
     *                                             the frame's bounds
     */
    @Override
    public Color color(int x, int y) {
        return new Color(FRAME.color(x, y));
    }

}
