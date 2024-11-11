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

package io.github.moremcmeta.moremcmeta.forge.impl.client.mixin;

import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.BoundTextureState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks this texture's names and ID.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(value = AbstractTexture.class, remap = false)
public abstract class AbstractTextureMixin implements NamedTexture {
    @Shadow
    private int id;

    @Unique
    private final Set<ResourceLocation> MOREMCMETA_NAMES = ConcurrentHashMap.newKeySet();

    @Unique
    @Override
    public void moremcmeta_addName(ResourceLocation name) {
        MOREMCMETA_NAMES.add(name);
    }

    @Unique
    @Override
    public Set<ResourceLocation> moremcmeta_names() {
        return MOREMCMETA_NAMES;
    }

    /**
     * Updates this texture's ID when a new one is generated.
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "getId()I", at = @At("RETURN"))
    public void moremcmeta_onGetId(CallbackInfoReturnable<Integer> callbackInfo) {
        BoundTextureState.TEXTURES_BY_ID.put(id, this);
    }

    /**
     * Delete this texture's ID when it is released
     * @param callbackInfo  callback info from Mixin
     */
    @Inject(method = "releaseId()V", at = @At("RETURN"))
    public void moremcmeta_onRelease(CallbackInfo callbackInfo) {
        BoundTextureState.TEXTURES_BY_ID.remove(id);
    }

}
