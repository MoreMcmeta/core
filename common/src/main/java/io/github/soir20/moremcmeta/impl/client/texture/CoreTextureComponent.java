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

import io.github.soir20.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.soir20.moremcmeta.api.client.texture.TextureComponent;

/**
 * Responds to events relevant to MoreMcmeta's internal implementation for a particular texture.
 * @author soir20
 */
public interface CoreTextureComponent
        extends TextureComponent<EventDrivenTexture.TextureAndFrameView, EventDrivenTexture.TextureAndFrameView> {

    /**
     * Responds to the registration event of the associated texture. Note that the lifetime of the
     * {@link CurrentFrameView} provided to this method is limited to the call of this method. Attempting
     * to retain and use a {@link CurrentFrameView} at a later point will cause a
     * {@link io.github.soir20.moremcmeta.api.client.texture.FrameView.IllegalFrameReference} exception to be thrown.
     * @param currentFrame      view of the texture's current frame
     * @param predefinedFrames  persistent views of predefined frames
     */
    default void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame,
                                FrameGroup<PersistentFrameView> predefinedFrames) {}

}
