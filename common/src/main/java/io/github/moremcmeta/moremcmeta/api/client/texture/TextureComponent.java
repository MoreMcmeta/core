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
 * Responds to basic events for a particular texture.
 * @param <V> type of texture view
 * @author soir20
 * @since 4.0.0
 */
public interface TextureComponent<V> {

    /**
     * Responds to the tick event of the associated texture. Note that the lifetime of the {@link CurrentFrameView}
     * provided to this method is limited to the call of this method. Attempting to retain and use a
     * {@link CurrentFrameView} at a later point will cause a {@link IllegalFrameReferenceException} exception
     * to be thrown.
     * @param currentFrame      view of the texture's current frame
     * @param predefinedFrames  persistent views of all predefined frames
     */
    default void onTick(V currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {}

    /**
     * Responds to the close event of the associated texture. Note that the lifetime of the {@link CurrentFrameView}
     * provided to this method is limited to the call of this method. Attempting to retain and use a
     * {@link CurrentFrameView} at a later point will cause a {@link IllegalFrameReferenceException} exception
     * to be thrown.
     * @param currentFrame      view of the texture's current frame
     * @param predefinedFrames  persistent views of all predefined frames
     */
    default void onClose(V currentFrame, FrameGroup<PersistentFrameView> predefinedFrames) {}

}
