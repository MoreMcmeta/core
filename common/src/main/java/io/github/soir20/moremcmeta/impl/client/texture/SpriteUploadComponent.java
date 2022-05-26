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

import io.github.soir20.moremcmeta.api.math.Point;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture to an atlas sprite.
 * @author soir20
 */
public class SpriteUploadComponent implements CoreTextureComponent {
    private final Sprite SPRITE;
    private final Point UPLOAD_POINT;

    /**
     * Creates a new component for uploading a texture to an atlas sprite.
     * The upload point of the sprite provided should not change at any
     * point in the future.
     * @param sprite        the sprite to upload the texture to
     */
    public SpriteUploadComponent(Sprite sprite) {
        SPRITE = requireNonNull(sprite, "Sprite cannot be null");
        UPLOAD_POINT = SPRITE.getUploadPoint();
    }

    /**
     * Releases unnecessary memory by lowering the frame's mipmap level to be the same as the sprite.
     * @param currentFrame      view of the texture's current frame
     */
    @Override
    public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame) {
        currentFrame.lowerMipmapLevel(SPRITE.getMipmapLevel());
    }

    /**
     * Uploads the texture when it needs to be uploaded.
     * @param currentFrame      view of the texture's current frame
     */
    @Override
    public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame) {
        currentFrame.uploadAt(UPLOAD_POINT);
    }

    /**
     * Uploads the texture to the atlas on tick since the sprite will never be bound.
     * @param currentFrame      view of the texture's current frame
     */
    @Override
    public void onTick(EventDrivenTexture.TextureAndFrameView currentFrame) {

        // We need this listener because atlas sprites will never be bound
        SPRITE.bind();
        currentFrame.getTexture().upload();

    }

}
