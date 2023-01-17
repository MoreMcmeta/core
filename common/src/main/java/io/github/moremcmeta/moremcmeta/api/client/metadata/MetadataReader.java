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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Reads metadata from a file or throws a {@link InvalidMetadataException} if it is not valid.
 * @author soir20
 * @since 4.0.0
 */
@FunctionalInterface
public interface MetadataReader {

    /**
     * Reads metadata from a file, provided as an input stream.
     *
     * This reader should *not* throw exceptions that are not {@link InvalidMetadataException}.
     * @param metadataLocation      location of the metadata file
     * @param metadataStream        data in the metadata file
     * @param resourceSearcher      searches for resources that exist in any currently-applied resource pack
     * @return an immutable view of the read metadata by texture path
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    Map<ResourceLocation, MetadataView> read(ResourceLocation metadataLocation, InputStream metadataStream,
                                             Function<Predicate<String>, Set<ResourceLocation>> resourceSearcher)
            throws InvalidMetadataException;

}
