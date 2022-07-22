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

package io.github.soir20.moremcmeta.api.client.metadata;

import java.io.InputStream;

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
     * @param metadataStream        data in the metadata file
     * @return an immutable view of the read metadata
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    MetadataView read(InputStream metadataStream) throws InvalidMetadataException;

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
