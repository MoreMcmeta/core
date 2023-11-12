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

import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.LocatedSpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Updates global sprite name mappings when the sprite loaders are created.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(SpriteResourceLoader.class)
public final class SpriteResourceLoaderMixin {
    @Shadow
    private List<SpriteSource> sources;

    /**
     * Updates global sprite name mappings when the sprite loaders are created.
     * @param resourceManager       Minecraft's resource manager
     * @param atlasName             corresponding atlas ID
     * @param callbackInfo          callback info from Mixin
     */
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;",
            at = @At("RETURN"))
    private static void moremcmeta_onLoad(ResourceManager resourceManager, ResourceLocation atlasName,
                                          CallbackInfoReturnable<SpriteResourceLoader> callbackInfo) {
        SpriteResourceLoaderMixin loader = (SpriteResourceLoaderMixin) (Object) callbackInfo.getReturnValue();
        ResourceLocation atlasLocation = atlasName.withPrefix("textures/atlas/").withSuffix(".png");
        loader.sources.forEach((source) -> {

            // PalettedPermutations and Unstitcher are not supported because they hard-code empty animation metadata
            if (source instanceof LocatedSpriteSource locatedSource) {
                locatedSource.moremcmeta_updateSpriteMappings(resourceManager, atlasLocation);
            }

        });
    }

}
