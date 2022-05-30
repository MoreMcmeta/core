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

package io.github.soir20.moremcmeta.impl.client.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads a texture.
 * @param <T>
 * @author soir20
 */
public interface TextureReader<T> {

    /**
     * Reads an animated texture from file data.
     * @param textureStream     input stream of image data
     * @param metadataStream    input stream of texture metadata (JSON)
     * @return getter for retrieving an animated texture after all resources are loaded
     * @throws IOException failure reading from either input stream
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    T read(InputStream textureStream, InputStream metadataStream) throws IOException, InvalidMetadataException;

    /**
     * Signals that the metadata provided to the reader is invalid for some reason. It may be
     * in an invalid format or have an incompatible combination of properties.
     * @author soir20
     */
    class InvalidMetadataException extends Exception {

        /**
         * Creates a new exception with a detail message.
         * @param reason    the reason the metadata is invalid
         */
        public InvalidMetadataException(String reason) {
            super(reason);
        }

    }

}
