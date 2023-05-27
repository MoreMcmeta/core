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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.NamedTexture;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Uploads all dependencies when this texture is bound.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(AbstractTexture.class)
public abstract class AbstractTextureMixin implements NamedTexture {
    @Unique
    private static AbstractTexture lastBound;
    @Unique
    private final Set<ResourceLocation> MOREMCMETA_NAMES = new HashSet<>();

    @Unique
    @Override
    public void moremcmeta_addName(ResourceLocation name) {
        onRenderThread(() -> MOREMCMETA_NAMES.add(name));
    }

    /**
     * Uploads all dependencies when this texture is bound.
     * @param callbackInfo      callback info from Mixin
     */
    @Inject(method = "bind()V", at = @At("RETURN"))
    public void moremcmeta_onBind(CallbackInfo callbackInfo) {
        onRenderThread(this::uploadDependencies);
    }

    /**
     * Ensures that all work is done on the same thread. This is a defense against any subtle
     * multithreading issues.
     * @param action    action to perform on the render thread
     */
    @Unique
    private void onRenderThread(Runnable action) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(action::run);
        } else {
            action.run();
        }
    }

    /**
     * Uploads all of a base texture's dependencies, assuming it is already bound.
     */
    @Unique
    private void uploadDependencies() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        MOREMCMETA_NAMES.forEach((base) -> {
            Set<ResourceLocation> dependencies = MoreMcmeta.dependencies(base);
            dependencies.forEach((dependency) -> {
                AbstractTexture texture = textureManager.getTexture(dependency, MissingTextureAtlasSprite.getTexture());

                if (texture instanceof EventDrivenTexture) {
                    ((EventDrivenTexture) texture).upload(base);
                }
            });
        });
    }

}
