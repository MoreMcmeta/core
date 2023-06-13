/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameIndexOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Basic implementation of a {@link FrameGroup} that holds a particular type of {@link FrameView}.
 * @param <F> type of {@link FrameView} available from this group
 * @author soir20
 */
public final class FrameGroupImpl<F extends FrameView> implements FrameGroup<F> {
    private final List<F> FRAMES;

    /**
     * Creates a new group of frames.
     * @param frames the frames to put in the group
     */
    public FrameGroupImpl(List<? extends F> frames) {
        requireNonNull(frames, "Frames cannot be null");

        if (frames.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("No frame can be null");
        }

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
                IntStream.range(0, requireNonNull(frames, "Frames cannot be null").size())
                .mapToObj(
                        (index) -> requireNonNull(viewConstructor, "View constructor cannot be null")
                                .apply(requireNonNull(frames.get(index), "No frame can be null"), index)
                ).collect(Collectors.toList())
        );
    }

    @Override
    public F frame(int index) {
        if (index < 0 || index >= FRAMES.size()) {
            throw new FrameIndexOutOfBoundsException(index);
        }

        return FRAMES.get(index);
    }

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
