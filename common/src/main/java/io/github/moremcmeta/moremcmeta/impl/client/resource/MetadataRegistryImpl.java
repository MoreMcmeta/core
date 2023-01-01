/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import io.github.moremcmeta.moremcmeta.impl.client.texture.Sprite;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link MetadataRegistry} that can be updated.
 * @author soir20
 */
public class MetadataRegistryImpl implements MetadataRegistry {
    private ImmutableMap<String, ImmutableMap<ResourceLocation, ParsedMetadata>> metadata;

    /**
     * Creates a new implementation of a {@link MetadataRegistry}.
     */
    public MetadataRegistryImpl() {
        metadata = ImmutableMap.of();
    }

    /**
     * Retrieves the metadata provided by the given plugin for the given texture.
     * @param pluginName            name of the plugin that provided the metadata
     * @param textureLocation       full (non-sprite, including .png suffix) location of the texture
     * @return the metadata provided by the given plugin for the given texture, if there is any
     */
    @Override
    public Optional<ParsedMetadata> metadataFromPath(String pluginName, ResourceLocation textureLocation) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(textureLocation, "Texture location cannot be null");
        return Optional.ofNullable(
                metadata.getOrDefault(pluginName, ImmutableMap.of())
                        .get(textureLocation)
        );
    }

    /**
     * Retrieves the metadata provided by the given plugin for the given texture.
     * @param pluginName            name of the plugin that provided the metadata
     * @param spriteName            name of the sprite (omitting the textures/ prefix and .png suffix)
     * @return the metadata provided by the given plugin for the given texture, if there is any
     */
    @Override
    public Optional<ParsedMetadata> metadataFromSpriteName(String pluginName, ResourceLocation spriteName) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(spriteName, "Sprite name cannot be null");
        return Optional.ofNullable(
                metadata.getOrDefault(pluginName, ImmutableMap.of())
                        .get(Sprite.makeTextureLocation(spriteName))
        );
    }

    /**
     * Retrieves all metadata associated with a given plugin.
     * @param pluginName            name of the plugin that provided the metadata
     * @return all metadata associated with a given plugin, by texture location, as an immutable map
     */
    @Override
    public Map<ResourceLocation, ParsedMetadata> metadataByPlugin(String pluginName) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        return metadata.getOrDefault(pluginName, ImmutableMap.of());
    }

    /**
     * Updates the current metadata in this registry. The old metadata is discarded.
     * @param textureData           all current texture data (by full texture location)
     */
    public void set(Map<? extends ResourceLocation, ? extends TextureData<?>> textureData) {
        requireNonNull(textureData, "Texture data cannot be null");

        Map<String, ImmutableMap.Builder<ResourceLocation, ParsedMetadata>> builders = new HashMap<>();

        for (Map.Entry<? extends ResourceLocation, ? extends TextureData<?>> entry : textureData.entrySet()) {
            ResourceLocation textureLocation = entry.getKey();

            for (Triple<String, ParsedMetadata, ComponentProvider> pluginEntry : entry.getValue().parsedMetadata()) {
                String pluginName = pluginEntry.getLeft();
                ParsedMetadata sectionData = pluginEntry.getMiddle();

                builders.computeIfAbsent(pluginName, (key) -> new ImmutableMap.Builder<>())
                        .put(textureLocation, sectionData);
            }
        }

        metadata = ImmutableMap.copyOf(
                Maps.transformValues(builders, ImmutableMap.Builder::build)
        );
    }

}
