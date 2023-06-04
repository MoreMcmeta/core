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

import io.github.moremcmeta.moremcmeta.api.client.texture.CurrentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.IllegalFrameReferenceException;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Responds to events relevant to MoreMcmeta's internal implementation for a particular texture.
 * @author soir20
 */
public interface CoreTextureComponent
        extends TextureComponent<EventDrivenTexture.TextureAndFrameView> {

    /**
     * Responds to the registration event of the associated texture. Note that the lifetime of the
     * {@link CurrentFrameView} provided to this method is limited to the call of this method. Attempting
     * to retain and use a {@link CurrentFrameView} at a later point will cause a
     * {@link IllegalFrameReferenceException} exception to be thrown.
     * @param currentFrame      view of the texture's current frame
     * @param predefinedFrames  persistent views of predefined frames
     */
    default void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame,
                                FrameGroup<? extends PersistentFrameView> predefinedFrames) {}

    /**
     * <p>Responds to the upload event of the associated texture. The upload event occurs when the texture is
     * updated with respect to OpenGL. The upload event does not necessarily occur for every transform applied
     * in other events. Note that the lifetime of the {@link UploadableFrameView} provided to this method is limited
     * to the call of this method. Attempting to retain and use a {@link UploadableFrameView} at a later point will
     * cause a {@link IllegalFrameReferenceException} exception to be thrown.</p>
     *
     * <p>Assume that the texture at the provided {@link ResourceLocation} is bound before this method is
     * called.</p>
     * @param currentFrame      view of the texture's current frame
     * @param baseLocation      location of the base to which frames will be uploaded
     */
    default void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {}

}
