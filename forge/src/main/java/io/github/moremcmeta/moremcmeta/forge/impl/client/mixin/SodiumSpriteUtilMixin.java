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

package io.github.moremcmeta.moremcmeta.forge.impl.client.mixin;

import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.LocatableSpriteAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds Sodium compatibility for sprite animations.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = {net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil.class, org.embeddedt.embeddium.api.render.texture.SpriteUtil.class},
        remap = false)
public class SodiumSpriteUtilMixin {

    /**
     * Queues sprites for update when Sodium marks them as active.
     * @param sprite        sprite to update
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "markSpriteActive(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V",
            at = @At("RETURN"), require = 0)
    private static void moremcmeta_onSodiumMarkSpriteActive(TextureAtlasSprite sprite, CallbackInfo callbackInfo) {
        AbstractTexture rawAtlas = Minecraft.getInstance().getTextureManager()
                .getTexture(sprite.atlasLocation());
        if (rawAtlas instanceof LocatableSpriteAtlas atlas) {
            atlas.moremcmeta_queueSpriteForUpdate(sprite.contents().name());
        }
    }

    /**
     * Tells Sodium to update sprites that have MoreMcmeta animations.
     * @param sprite        sprite to update
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "hasAnimation(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private static void moremcmeta_onSodiumSpriteHasAnimation(TextureAtlasSprite sprite, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!MoreMcmeta.dependencies(sprite.contents().name()).isEmpty()) {
            callbackInfo.setReturnValue(true);
        }
    }

}
