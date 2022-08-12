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

package io.github.soir20.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.api.client.metadata.MetadataRegistry;
import io.github.soir20.moremcmeta.api.client.metadata.ParsedMetadata;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;
import io.github.soir20.moremcmeta.impl.client.io.TextureData;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.Optional;

import static io.github.soir20.moremcmeta.impl.client.texture.Sprite.makeTextureLocation;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link MetadataRegistry} that can be updated.
 * @author soir20
 */
public class MetadataRegistryImpl implements MetadataRegistry {
    private ImmutableMap<Pair<String, ResourceLocation>, ParsedMetadata> metadata;

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
    public Optional<ParsedMetadata> getFromPath(String pluginName, ResourceLocation textureLocation) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(textureLocation, "Texture location cannot be null");
        return Optional.ofNullable(metadata.get(Pair.of(pluginName, textureLocation)));
    }

    /**
     * Retrieves the metadata provided by the given plugin for the given texture.
     * @param pluginName            name of the plugin that provided the metadata
     * @param spriteName            name of the sprite (omitting the textures/ prefix and .png suffix)
     * @return the metadata provided by the given plugin for the given texture, if there is any
     */
    @Override
    public Optional<ParsedMetadata> getFromSpriteName(String pluginName, ResourceLocation spriteName) {
        requireNonNull(pluginName, "Plugin name cannot be null");
        requireNonNull(spriteName, "Sprite name cannot be null");
        return Optional.ofNullable(metadata.get(Pair.of(pluginName, makeTextureLocation(spriteName))));
    }

    /**
     * Updates the current metadata in this registry. The old metadata is discarded.
     * @param textureData           all current texture data (by full texture location)
     */
    public void set(Map<? extends ResourceLocation, ? extends TextureData<?>> textureData) {
        requireNonNull(textureData, "Texture data cannot be null");

        ImmutableMap.Builder<Pair<String, ResourceLocation>, ParsedMetadata> builder = ImmutableMap.builder();

        for (Map.Entry<? extends ResourceLocation, ? extends TextureData<?>> entry : textureData.entrySet()) {
            ResourceLocation textureLocation = entry.getKey();

            for (Triple<String, ParsedMetadata, ComponentProvider> pluginEntry : entry.getValue().parsedMetadata()) {
                String pluginName = pluginEntry.getLeft();
                ParsedMetadata sectionData = pluginEntry.getMiddle();

                builder.put(
                        Pair.of(pluginName, textureLocation),
                        sectionData
                );
            }
        }

        metadata = builder.build();
    }

}
