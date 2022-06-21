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

import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * Basic implementation of a {@link FrameGroup} that holds a particular type of {@link FrameView}.
 * @param <F> type of {@link FrameView} available from this group
 * @author soir20
 */
public class FrameGroupImpl<F extends FrameView> implements FrameGroup<F> {
    private final List<F> FRAMES;

    /**
     * Creates a new group of frames.
     * @param frames the frames to put in the group
     */
    public FrameGroupImpl(List<? extends F> frames) {
        FRAMES = new ArrayList<>(frames);
    }

    /**
     * Creates a new group of frames, along with persistent wrappers.
     * @param frames            the frames to put in the group
     * @param viewConstructor   converts a frame and its index to the wrapper
     * @param <T> original type of the frames
     */
    public <T> FrameGroupImpl(List<? extends T> frames,
                              BiFunction<? super T, ? super Integer, ? extends F> viewConstructor) {
        this(
                IntStream.range(0, frames.size())
                .mapToObj((index) -> viewConstructor.apply(frames.get(index), index))
                .toList()
        );
    }

    /**
     * Gets a frame in this group by its index.
     * @param index     index of the frame to retrieve
     * @return the frame at this index in the group
     * @throws FrameView.FrameIndexOutOfBoundsException if the provided index is outside
     *                                                  the range of legal frame indices
     */
    @Override
    public F frame(int index) {
        if (index < 0 || index >= FRAMES.size()) {
            throw new FrameView.FrameIndexOutOfBoundsException(index);
        }

        return FRAMES.get(index);
    }

    /**
     * Gets the number of frames in this group.
     * @return number of frames in this group
     */
    @Override
    public int frames() {
        return FRAMES.size();
    }

    /**
     * Gets an iterator for the views in this group.
     * @return an iterator for the views in this group
     */
    @NotNull
    @Override
    public Iterator<F> iterator() {
        return FRAMES.iterator();
    }

}
