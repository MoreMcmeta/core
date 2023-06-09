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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import io.github.moremcmeta.moremcmeta.impl.client.MoreMcmeta;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * Contains the current metadata for all textures, if any.
 * @author soir20
 * @since 4.0.0
 */
public interface MetadataRegistry {

    /**
     * The global {@link MetadataRegistry}. This metadata registry returned may contain
     * different metadata after each resource reload. The registry is updated *before* the "load" stage
     * of resource reloading, so it is safe to access from a reload listener.
     */
    MetadataRegistry INSTANCE = MoreMcmeta.METADATA_REGISTRY;

    /**
     * Retrieves the metadata provided by the given plugin for the given texture.
     * @param pluginName            name of the plugin that provided the metadata
     * @param textureLocation       full (non-sprite, including .png suffix) location of the texture
     * @return the metadata provided by the given plugin for the given texture, if there is any
     */
    Optional<AnalyzedMetadata> metadataFromPath(String pluginName, ResourceLocation textureLocation);

    /**
     * Retrieves the metadata provided by the given plugin for the given texture.
     * @param pluginName            name of the plugin that provided the metadata
     * @param spriteName            name of the sprite (omitting the textures/ prefix and .png suffix)
     * @return the metadata provided by the given plugin for the given texture, if there is any
     */
    Optional<AnalyzedMetadata> metadataFromSpriteName(String pluginName, ResourceLocation spriteName);

    /**
     * Retrieves all metadata associated with a given plugin.
     * @param pluginName            name of the plugin that provided the metadata
     * @return all metadata associated with a given plugin, by texture location, as an immutable map
     */
    Map<? extends ResourceLocation, ? extends AnalyzedMetadata> metadataByPlugin(String pluginName);

}
