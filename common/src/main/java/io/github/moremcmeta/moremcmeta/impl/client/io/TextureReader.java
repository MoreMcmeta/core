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

package io.github.moremcmeta.moremcmeta.impl.client.io;

import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads a texture.
 * @param <T>
 * @author soir20
 */
@FunctionalInterface
public interface TextureReader<T> {

    /**
     * Reads a texture from file data.
     * @param textureStream     input stream of image data
     * @param metadata          metadata associated with this texture
     * @return texture read from the stream
     * @throws IOException failure reading from either input stream
     * @throws InvalidMetadataException if the metadata is not valid for some reason
     */
    T read(InputStream textureStream, MetadataView metadata) throws IOException, InvalidMetadataException;

}
