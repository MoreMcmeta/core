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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Map;

/**
 * Reads metadata from a file or throws a {@link InvalidMetadataException} if it is not valid.
 * @author soir20
 * @since 4.0.0
 */
public interface MetadataParser {

    /**
     * <p>Reads metadata from a file, provided as an input stream. The order of keys in each {@link MetadataView}
     * matters, as it determines in what order MoreMcmeta will apply plugins.</p>
     *
     * <p>This reader should *not* throw exceptions that are not {@link InvalidMetadataException}.</p>
     *
     * <p><b>This method may be called from multiple threads concurrently. If there is any state shared between
     * calls, it must be synchronized properly for concurrent usage.</b></p>
     * @param metadataLocation      location of the metadata file
     * @param metadataStream        data in the metadata file
     * @param resourceRepository    searches for resources that exist in any currently-applied resource pack
     * @return an immutable view of the read metadata by texture path
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    Map<? extends ResourceLocation, ? extends MetadataView> parse(ResourceLocation metadataLocation,
                                                                  InputStream metadataStream,
                                                                  ResourceRepository resourceRepository)
            throws InvalidMetadataException;

    /**
     * <p>Parses all metadata at the root of the given resource pack. The given pack may or may not be currently
     * selected, allowing for resource pack icons to have special effects in the pack options screen.</p>
     *
     * <p>This method should throw no exceptions. If it encounters invalid metadata, this method is responsible
     * for logging the issue for the user.</p>
     *
     * <p><b>This method may be called from multiple threads concurrently. If there is any state shared between
     * calls, it must be synchronized properly for concurrent usage.</b></p>
     * @param pack      pack to parse the root metadata of
     * @return a map with metadata locations as keys, and maps from texture locations to metadata as values
     * @since 4.2.0
     */
    default Map<? extends RootResourceName, ? extends Map<? extends RootResourceName, ? extends MetadataView>> parse(
            ResourceRepository.Pack pack) {
        return ImmutableMap.of();
    }

    /**
     * <p>Combines multiple {@link MetadataView}s for the same texture into one {@link MetadataView}. All
     * views provided to this method are guaranteed to come from this reader's
     * {@link MetadataParser#parse(ResourceLocation, InputStream, ResourceRepository)} method.</p>
     *
     * <p>As with the {@link MetadataParser#parse(ResourceLocation, InputStream, ResourceRepository)} method,
     * the order of keys in the resultant {@link MetadataView} matters. The order determines in what order
     * MoreMcmeta will apply plugins.</p>
     *
     * <p>This reader should *not* throw exceptions that are not {@link InvalidMetadataException}.</p>
     *
     * <p><b>This method may be called from multiple threads concurrently. If there is any state shared between
     * calls, it must be synchronized properly for concurrent usage.</b></p>
     * @param textureLocation               full path of the texture whose metadata should be combined
     * @param metadataByLocation            metadata by metadata file location
     * @return combined {@link MetadataView} with all metadata
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    default MetadataView combine(ResourceLocation textureLocation,
                                 Map<? extends ResourceLocation, ? extends MetadataView> metadataByLocation)
            throws InvalidMetadataException {
        throw new InvalidMetadataException("Format does not support metadata split among multiple files");
    }

}
