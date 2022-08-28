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

import static java.util.Objects.requireNonNull;

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
     * @return an immutable view of the read metadata
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    ReadMetadata read(ResourceLocation metadataLocation, InputStream metadataStream) throws InvalidMetadataException;

    /**
     * Contains the result of {@link MetadataReader#read(ResourceLocation, InputStream)} if it was successful.
     * @author soir20
     * @since 4.0.0
     */
    final class ReadMetadata {
        private final ResourceLocation TEXTURE_LOCATION;
        private final MetadataView METADATA;

        /**
         * Creates a new container for the results of metadata reading.
         * @param textureLocation       location of the texture associated with this metadata
         * @param metadata              a view of the metadata
         */
        public ReadMetadata(ResourceLocation textureLocation, MetadataView metadata) {
            TEXTURE_LOCATION = requireNonNull(textureLocation, "Texture location cannot be null");
            METADATA = requireNonNull(metadata, "Metadata cannot be null");
        }

        /**
         * Gets the location of the texture associated with this metadata.
         * @return the location of the texture associated with this metadata
         */
        public ResourceLocation textureLocation() {
            return TEXTURE_LOCATION;
        }

        /**
         * Gets the metadata that was read.
         * @return the metadata that was read
         */
        public MetadataView metadata() {
            return METADATA;
        }

    }

    /**
     * Signals that the metadata provided to the reader is invalid for some reason. It may be
     * in an invalid format or have an incompatible combination of properties.
     * @author soir20
     * @since 4.0.0
     */
    final class InvalidMetadataException extends Exception {

        /**
         * Creates a new exception with a detail message.
         * @param reason    the reason the metadata is invalid
         */
        public InvalidMetadataException(String reason) {
            super(reason);
        }

    }

}
