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

package io.github.moremcmeta.moremcmeta.impl.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.moremcmeta.moremcmeta.impl.client.texture.BoundTextureState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Updates the {@link BoundTextureState} when the OpenGL state manager is used.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = GlStateManager.class, remap = false)
public class GlStateManagerMixin {
    @Shadow
    private static int activeTexture;

    /**
     * Removes a single texture ID when deleted.
     * @param textureId     texture ID to remove
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "_deleteTexture(I)V", at = @At("HEAD"))
    private static void moremcmeta_onDeleteTexture(int textureId, CallbackInfo callbackInfo) {
        for (int index = 0; index < BoundTextureState.BOUND_TEXTURES.length; index++) {
            if (BoundTextureState.BOUND_TEXTURES[index] == textureId) {
                BoundTextureState.BOUND_TEXTURES[index] = -1;
            }
        }
    }

    /**
     * Removes all textures from a list of texture IDs.
     * @param textureIds    texture IDs to delete
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "_deleteTextures([I)V", at = @At("HEAD"))
    private static void moremcmeta_onDeleteTextures(int[] textureIds, CallbackInfo callbackInfo) {
        for (int index = 0; index < BoundTextureState.BOUND_TEXTURES.length; index++) {
            for (int textureId : textureIds) {
                if (BoundTextureState.BOUND_TEXTURES[index] == textureId) {
                    BoundTextureState.BOUND_TEXTURES[index] = -1;
                    break;
                }
            }
        }
    }

    /**
     * Updates the bound texture ID when a new texture is bound.
     * @param textureId     texture ID to bind
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "_bindTexture(I)V", at = @At("HEAD"))
    private static void moremcmeta_onBindTexture(int textureId, CallbackInfo callbackInfo) {
        BoundTextureState.BOUND_TEXTURES[activeTexture] = textureId;
    }
}
