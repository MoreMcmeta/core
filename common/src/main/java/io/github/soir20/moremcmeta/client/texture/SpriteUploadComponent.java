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

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture to an atlas sprite.
 * @author soir20
 */
public class SpriteUploadComponent implements TextureComponent {
    private final Sprite SPRITE;

    /**
     * Creates a new component for uploading a texture to an atlas sprite.
     * The upload point of the sprite provided should not change at any
     * point in the future.
     * @param sprite        the sprite to upload the texture to
     */
    public SpriteUploadComponent(Sprite sprite) {
        SPRITE = requireNonNull(sprite, "Sprite cannot be null");
    }

    /**
     * Gets the listeners for this component. Sprite will be bound and uploaded
     * on every tick, in addition to regularly-triggered uploads.
     * @return all the listeners for this component
     */
    @Override
    public Stream<TextureListener> getListeners() {
        Point uploadPoint = SPRITE.getUploadPoint();

        TextureListener uploadListener = new TextureListener(
                TextureListener.Type.UPLOAD,
                (state) -> {
                    state.getImage().lowerMipmapLevel(SPRITE.getMipmapLevel());
                    state.getImage().uploadAt(uploadPoint);
                }
        );

        // We need this listener because atlas sprites will never be bound
        TextureListener tickListener = new TextureListener(
                TextureListener.Type.TICK,
                (state) -> {
                    SPRITE.bind();
                    state.getTexture().upload();
                });

        return Stream.of(uploadListener, tickListener);
    }

}
