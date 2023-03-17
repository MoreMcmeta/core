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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;

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

        if (index < 0) {
            throw new IllegalArgumentException("Predefined frame index cannot be negative");
        }

        INDEX = index;
    }

    @Override
    public int width() {
        return FRAME.width();
    }

    @Override
    public int height() {
        return FRAME.height();
    }

    @Override
    public Optional<Integer> index() {
        return Optional.of(INDEX);
    }

    @Override
    public int color(int x, int y) {
        return FRAME.color(x, y);
    }

}
