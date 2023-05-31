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

package io.github.moremcmeta.moremcmeta.api.client.metadata;

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
     * @param metadataLocation      location of the metadata file
     * @param metadataStream        data in the metadata file
     * @param resourceRepository    searches for resources that exist in any currently-applied resource pack
     * @return an immutable view of the read metadata by texture path
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    Map<ResourceLocation, MetadataView> parse(ResourceLocation metadataLocation, InputStream metadataStream,
                                              ResourceRepository resourceRepository)
            throws InvalidMetadataException;

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
     * @param textureLocation               full path of the texture whose metadata should be combined
     * @param metadataByLocation            metadata by metadata file location
     * @return combined {@link MetadataView} with all metadata
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    default MetadataView combine(ResourceLocation textureLocation, Map<ResourceLocation, MetadataView> metadataByLocation)
            throws InvalidMetadataException {
        throw new InvalidMetadataException("Format does not support metadata split among multiple files");
    }

}
