/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Allows textures used through {@link RenderSystem#_setShaderTexture(int, int)} to be updated,
 * even though they are not bound normally.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(RenderSystem.class)
public final class RenderSystemMixin {

    /**
     * Binds a texture when it is set as a shader texture. This method is usually called by GUI
     * methods, rather than binding the texture normally.
     * @param shaderIndex           index of the shader texture to set
     * @param textureLocation       location of the texture to bind
     * @param callbackInfo          callback info from Mixin
     */
    @Inject(method = "_setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void moremcmeta_onSetShaderTexture(int shaderIndex, ResourceLocation textureLocation,
                                                     CallbackInfo callbackInfo) {
        Minecraft.getInstance().getTextureManager().getTexture(textureLocation).bind();
    }

}
