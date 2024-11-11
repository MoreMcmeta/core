/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import org.lwjgl.opengl.GL32C;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the currently-bound texture.
 * @author soir20
 */
public final class BoundTextureState {

    /**
     * Tracks textures by their ID.
     */
    public static final ConcurrentHashMap<Integer, NamedTexture> TEXTURES_BY_ID = new ConcurrentHashMap<>();

    /**
     * Tracks texture IDs that are bound in each texture unit.
     */
    public static final int[] BOUND_TEXTURES = new int[GlStateManager.TEXTURE_COUNT];

    /**
     * Gets the bound texture in the active slot.
     * @return bound texture in the active slot
     */
    public static Optional<NamedTexture> currentTexture() {
        int textureId = BOUND_TEXTURES[GlStateManager._getActiveTexture() - GL32C.GL_TEXTURE0];
        if (textureId == -1) {
            return Optional.empty();
        }

        if (textureId >= 0) {
            return Optional.ofNullable(TEXTURES_BY_ID.get(textureId));
        }

        return Optional.empty();
    }

    /**
     * Prevents the state from being constructed.
     */
    private BoundTextureState() {}

}
