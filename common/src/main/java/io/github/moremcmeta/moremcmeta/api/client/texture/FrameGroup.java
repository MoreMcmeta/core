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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import org.jetbrains.annotations.ApiStatus;

/**
 * An ordered collection of {@link FrameView}s, which are accessible by index.
 * @param <F> type of {@link FrameView} available from this group
 * @author soir20
 * @since 4.0.0
 */
@ApiStatus.NonExtendable
public interface FrameGroup<F extends FrameView> extends Iterable<F> {

    /**
     * Gets a frame in this group by its index.
     * @param index     index of the frame to retrieve
     * @return the frame at this index in the group
     * @throws FrameIndexOutOfBoundsException if the provided index is outside
     *                                                  the range of legal frame indices
     */
    F frame(int index);

    /**
     * Gets the number of frames in this group.
     * @return number of frames in this group
     */
    int frames();

}
