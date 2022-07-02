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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.math.Point;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class SingleUploadComponent implements TextureComponent {
    private final TexturePreparer PREPARER;
    private final AtomicBoolean IS_PREPARED;

    /**
     * Creates a new upload component for an independent texture.
     * @param preparer      prepares the texture for OpenGL on registration
     */
    public SingleUploadComponent(TexturePreparer preparer) {
        PREPARER = requireNonNull(preparer, "Preparer cannot be null");
        IS_PREPARED = new AtomicBoolean();
    }

    /**
     * Gets the listeners for this component.
     * @return all the listeners for this component
     */
    @Override
    public Stream<TextureListener> getListeners() {
        TextureListener registrationListener = new TextureListener(TextureListener.Type.REGISTRATION,
                (state) -> {
                    RGBAImageFrame image = state.getImage();
                    if (!RenderSystem.isOnRenderThreadOrInit()) {
                        RenderSystem.recordRenderCall(() -> {
                            PREPARER.prepare(
                                    state.getTexture().getId(),
                                    0,
                                    image.getWidth(),
                                    image.getHeight());
                            IS_PREPARED.set(true);
                        });
                    } else {
                        PREPARER.prepare(state.getTexture().getId(), 0, image.getWidth(), image.getHeight());
                        IS_PREPARED.set(true);
                    }
                });

        Point uploadPoint = new Point(0, 0);
        TextureListener uploadListener = new TextureListener(
                TextureListener.Type.UPLOAD,
                (state) -> {
                    if (IS_PREPARED.get()) {
                        state.getImage().lowerMipmapLevel(0);
                        state.getImage().uploadAt(uploadPoint);
                    }
                }
        );

        return Stream.of(registrationListener, uploadListener);
    }

}
