/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2023 soir20
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

import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Makes {@link AbstractTexture}s track their own names as they are registered to the {@link TextureManager}.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    /**
     * Makes {@link AbstractTexture}s track their own names as they are registered to the {@link TextureManager}.
     * @param location          location of the texture being registered
     * @param texture           texture being registered
     * @param callbackInfo      callback information from Mixin
     */
    @Inject(method = "register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",
            at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void moremcmeta_onRegister(ResourceLocation location, AbstractTexture texture, CallbackInfo callbackInfo) {
        ((NamedTexture) texture).moremcmeta_addName(location);
    }

}
