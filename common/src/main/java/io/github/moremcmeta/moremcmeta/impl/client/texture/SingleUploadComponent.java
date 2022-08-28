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
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                prepareTexture(currentFrame);
                IS_PREPARED.set(true);
            });
        } else {
            prepareTexture(currentFrame);
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
     * @param currentFrame      the current frame of the texture
     */
    private void prepareTexture(EventDrivenTexture.TextureAndFrameView currentFrame) {
        PREPARER.prepare(currentFrame.texture().getId(), 0, currentFrame.width(), currentFrame.height());
        currentFrame.lowerMipmapLevel(0);
    }

}
