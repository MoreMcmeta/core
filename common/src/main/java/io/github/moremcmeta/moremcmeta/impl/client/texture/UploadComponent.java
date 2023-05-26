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
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

/**
 * Manages uploading a texture that is not associated with an atlas sprite.
 * @author soir20
 */
public class UploadComponent implements CoreTextureComponent {
    private final TexturePreparer PREPARER;
    private final AtomicBoolean IS_PREPARED;
    private final BaseCollection BASE_DATA;

    /**
     * Creates a new upload component for an independent texture.
     * @param preparer      prepares the texture for OpenGL on registration
     * @param baseData      data about all bases for this texture
     */
    public UploadComponent(TexturePreparer preparer, BaseCollection baseData) {
        PREPARER = requireNonNull(preparer, "Preparer cannot be null");
        IS_PREPARED = new AtomicBoolean();
        BASE_DATA = requireNonNull(baseData, "Base data cannot be null");
    }

    @Override
    public void onRegistration(EventDrivenTexture.TextureAndFrameView currentFrame,
                               FrameGroup<PersistentFrameView> predefinedFrames) {

        /* Ensure the current frame is only accessed in this method, as the
           view may be invalidated if accessing them inside a render call. */

        // Remove unused mipmaps
        currentFrame.lowerMipmapLevel(BASE_DATA.maxMipmap());


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

    @Override
    public void onUpload(EventDrivenTexture.TextureAndFrameView currentFrame, ResourceLocation baseLocation) {
        if (!IS_PREPARED.get()) {
            return;
        }

        BASE_DATA.baseData(baseLocation).forEach((base) -> {
            long uploadPoint = base.uploadPoint();
            currentFrame.upload(Point.x(uploadPoint), Point.y(uploadPoint), base.mipmap());
        });
    }

    /**
     * Prepares an individual texture on the current thread.
     * @param texture      texture to prepare
     * @param frameWidth   width of a frame in the texture
     * @param frameHeight  height of a frame in the texture
     */
    private void prepareTexture(EventDrivenTexture texture, int frameWidth, int frameHeight) {
        PREPARER.prepare(texture.getId(), EventDrivenTexture.SELF_MIPMAP_LEVEL, frameWidth, frameHeight);
    }

}
