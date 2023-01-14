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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import io.github.moremcmeta.moremcmeta.api.math.Point;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class SingleUploadComponent implements CoreTextureComponent {
    private static final Point UPLOAD_POINT = new Point(0, 0);
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
     * Prepares an OpenGL image for the texture when it is registered.
     * @param currentFrame      view of the texture's current frame
     * @param predefinedFrames  persistent views of predefined frames
     */
    @Override
    public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame,
                               FrameGroup<PersistentFrameView> predefinedFrames) {

        /* Ensure the current frame is only accessed in this method, as the
           view may be invalidated if accessing them inside a render call. */
        currentFrame.lowerMipmapLevel(0);
        EventDrivenTexture texture = currentFrame.texture();
        int frameWidth = currentFrame.width();
        int frameHeight = currentFrame.height();

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                prepareTexture(texture, frameWidth, frameHeight);
                IS_PREPARED.set(true);
            });
        } else {
            prepareTexture(texture, frameWidth, frameHeight);
            IS_PREPARED.set(true);
        }
    }

    /**
     * Uploads the texture when it needs to be uploaded.
     * @param currentFrame      view of the texture's current frame
     */
    @Override
    public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame) {
        if (IS_PREPARED.get()) {
            currentFrame.upload(UPLOAD_POINT.x(), UPLOAD_POINT.y());
        }
    }

    /**
     * Prepares an individual texture on the current thread.
     * @param texture      texture to prepare
     * @param frameWidth   width of a frame in the texture
     * @param frameHeight  height of a frame in the texture
     */
    private void prepareTexture(EventDrivenTexture texture, int frameWidth, int frameHeight) {
        PREPARER.prepare(texture.getId(), 0, frameWidth, frameHeight);
    }

}
