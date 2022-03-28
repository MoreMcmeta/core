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

import io.github.soir20.moremcmeta.api.client.texture.TextureListener;
import io.github.soir20.moremcmeta.api.math.Point;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class SingleUploadComponent implements GenericTextureComponent<EventDrivenTexture.TextureState> {
    private final TexturePreparer PREPARER;

    /**
     * Creates a new upload component for an independent texture.
     * @param preparer      prepares the texture for OpenGL on registration
     */
    public SingleUploadComponent(TexturePreparer preparer) {
        PREPARER = requireNonNull(preparer, "Preparer cannot be null");
    }

    /**
     * Gets the listeners for this component.
     * @return all the listeners for this component
     */
    @Override
    public Stream<TextureListener<? super EventDrivenTexture.TextureState>> getListeners() {
        TextureListener<EventDrivenTexture.TextureState> registrationListener = new TextureListener<>(
                TextureListener.Type.REGISTRATION,
                (state) -> PREPARER.prepare(state.getTexture().getId(), 0, state.width(), state.height()));

        Point uploadPoint = new Point(0, 0);
        TextureListener<EventDrivenTexture.TextureState> uploadListener = new TextureListener<>(
                TextureListener.Type.UPLOAD,
                (state) -> {
                    state.lowerMipmapLevel(0);
                    state.uploadAt(uploadPoint);
                }
        );

        return Stream.of(registrationListener, uploadListener);
    }

}
