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

import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.client.texture.TextureHandle;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture to an atlas sprite.
 * @author soir20
 */
public class SpriteUploadComponent extends SingleUploadComponent {
    private final Sprite SPRITE;
    private final int UPLOAD_X;
    private final int UPLOAD_Y;

    /**
     * Creates a new component for uploading a texture to an atlas sprite.
     * The upload point of the sprite provided should not change at any
     * point in the future.
     * @param sprite        the sprite to upload the texture to
     * @param preparer      prepares the texture for OpenGL on registration
     */
    public SpriteUploadComponent(Sprite sprite, TexturePreparer preparer) {
        super(preparer, requireNonNull(sprite, "Sprite cannot be null").mipmapLevel());
        SPRITE = sprite;
        UPLOAD_X = Point.x(SPRITE.uploadPoint());
        UPLOAD_Y = Point.y(SPRITE.uploadPoint());
    }

    @Override
    public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame,
                         Function<ResourceLocation, Collection<TextureHandle>> textureLookup) {
        super.onUpload(currentFrame, textureLookup);
        SPRITE.bind();
        currentFrame.upload(UPLOAD_X, UPLOAD_Y);
    }

    @Override
    public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame,
                       FrameGroup<PersistentFrameView> predefinedFrames) {
        super.onTick(currentFrame, predefinedFrames);

        // We need this listener because atlas sprites will never be bound
        currentFrame.texture().upload();

    }

}
