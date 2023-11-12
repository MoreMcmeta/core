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

import io.github.moremcmeta.moremcmeta.impl.client.adapter.AtlasAdapter;
import io.github.moremcmeta.moremcmeta.impl.client.mixinaccess.LocatedSpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds name mappings from a {@link DirectoryLister} sprite source.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(DirectoryLister.class)
public final class DirectoryListerMixin implements LocatedSpriteSource {
    @Shadow
    @Final
    private String sourcePath;
    @Shadow
    @Final
    private String idPrefix;

    @Unique
    @Override
    public void moremcmeta_updateSpriteMappings(ResourceManager resourceManager, ResourceLocation atlasLocation) {
        FileToIdConverter fileToIdConverter = new FileToIdConverter("textures/" + sourcePath, ".png");
        fileToIdConverter.listMatchingResources(resourceManager).keySet().forEach((fullPath) -> {
            ResourceLocation spriteName = fileToIdConverter.fileToId(fullPath).withPrefix(idPrefix);
            AtlasAdapter.addNameMapping(atlasLocation, fullPath, spriteName);
        });
    }
}
