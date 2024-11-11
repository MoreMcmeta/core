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

import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.LocatableSpriteAtlas;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import io.github.moremcmeta.moremcmeta.impl.fabricapi.SpriteFinder;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Updates the cached sprite finder and tracks sprites that need to be updated.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(TextureAtlas.class)
public class TextureAtlasMixin implements LocatableSpriteAtlas {
    @Shadow
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName;

    @Unique
    private final LinkedBlockingQueue<ResourceLocation> SPRITES_TO_UPDATE = new LinkedBlockingQueue<>();
    @Unique
    private SpriteFinder spriteFinder;

    @Unique
    public void moremcmeta_resetSpriteFinder() {
        spriteFinder = new SpriteFinder(texturesByName);
    }

    @Unique
    public Optional<TextureAtlasSprite> moremcmeta_findSprite(float centerU, float centerV) {
        if (spriteFinder == null) {
            return Optional.empty();
        }

        return spriteFinder.find(centerU, centerV);
    }

    @Unique
    @Override
    public void moremcmeta_queueSpriteForUpdate(ResourceLocation spriteName) {
        SPRITES_TO_UPDATE.add(spriteName);
    }

    /**
     * Tells MoreMcmeta to update the queued updates. The texture atlas is already bound by the caller.
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "cycleAnimationFrames()V", at = @At("RETURN"))
    private void moremcmeta_onCycleAnimationFrames(CallbackInfo callbackInfo) {
        Set<ResourceLocation> spritesToUpdate = new HashSet<>();
        SPRITES_TO_UPDATE.drainTo(spritesToUpdate);
        NamedTexture.uploadDependencies(spritesToUpdate);
    }

}
