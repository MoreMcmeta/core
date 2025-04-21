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

package io.github.moremcmeta.moremcmeta.impl.client.mixinaccess;

import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import io.github.moremcmeta.moremcmeta.impl.client.texture.EventDrivenTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

/**
 * A texture that tracks its own name(s).
 * @author soir20
 */
public interface NamedTexture {

    /**
     * Adds a name to this texture.
     * @param name      name to add to this texture
     */
    void moremcmeta_addName(ResourceLocation name);

    /**
     * Gets all of this texture's names.
     * @return all of this texture's names
     */
    Set<ResourceLocation> moremcmeta_names();

    /**
     * Uploads all of a base texture's dependencies, assuming it is already bound.
     * @param textureNames  textures to update if necessary
     */
    @Unique
    static void uploadDependencies(Set<ResourceLocation> textureNames) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        textureNames.forEach((base) -> {
            Set<ResourceLocation> dependencies = MoreMcmeta.dependencies(base);
            dependencies.forEach((dependency) -> {
                ExtendedTextureManager extendedTextureManager = ((ExtendedTextureManager) textureManager);
                extendedTextureManager.texture(dependency).ifPresent((texture) -> {
                    if (texture instanceof EventDrivenTexture) {
                        ((EventDrivenTexture) texture).upload(base);
                    }
                });
            });
        });
    }

}
