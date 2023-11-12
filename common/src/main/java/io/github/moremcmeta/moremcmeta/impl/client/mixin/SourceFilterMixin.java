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
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ResourceLocationPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Removes name mappings as dictated by a {@link SourceFilter} sprite source.
 * @author soir20
 */
@SuppressWarnings("unused")
@Mixin(SourceFilter.class)
public class SourceFilterMixin implements LocatedSpriteSource {
    @Shadow
    @Final
    private ResourceLocationPattern filter;

    @Override
    public void moremcmeta_updateSpriteMappings(ResourceManager resourceManager, ResourceLocation atlasLocation) {
        AtlasAdapter.removeNameMappings(atlasLocation, filter.locationPredicate());
    }
}
