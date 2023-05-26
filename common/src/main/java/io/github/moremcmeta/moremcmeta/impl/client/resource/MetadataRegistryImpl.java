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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.moremcmeta.moremcmeta.api.client.metadata.Base;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;
import io.github.moremcmeta.moremcmeta.api.client.texture.SpriteName;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
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

    @Override
    public Optional<ParsedMetadata> metadataFromPath(String pluginName, ResourceLocation textureLocation) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(textureLocation, "Texture location cannot be null");
        return Optional.ofNullable(
                requireNonNull(metadata.getOrDefault(pluginName, ImmutableMap.of()), "Metadata should never be null")
                        .get(textureLocation)
        );
    }

    @Override
    public Optional<ParsedMetadata> metadataFromSpriteName(String pluginName, ResourceLocation spriteName) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(spriteName, "Sprite name cannot be null");

        /* If the sprite name is actually a texture location, toTexturePath() would return that name, and
           the metadata would be retrieved. For consistency, this method should return Optional.empty()
           when the provided location is not a sprite name since metadataFromPath() does not work with
           sprite names.*/
        if (!SpriteName.isSpriteName(spriteName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                requireNonNull(metadata.getOrDefault(pluginName, ImmutableMap.of()), "Metadata should never be null")
                        .get(SpriteName.toTexturePath(spriteName))
        );
    }

    @Override
    public Map<ResourceLocation, ParsedMetadata> metadataByPlugin(String pluginName) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        return metadata.getOrDefault(pluginName, ImmutableMap.of());
    }

    /**
     * Retrieves all bases associated with the given texture from any plugin. Duplicates may appear.
     * @param textureLocation       full path of the texture whose bases to search for
     * @return all bases associated with the given texture as defined in metadata
     */
    public Collection<Base> bases(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");

        ImmutableList.Builder<Base> builder = new ImmutableList.Builder<>();
        Collection<String> plugins = metadata.keySet();

        for (String plugin : plugins) {
            metadataFromPath(plugin, textureLocation).ifPresent((metadata) -> builder.addAll(metadata.bases()));
        }

        return builder.build();
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
