/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentBuilder;

/**
 * Analyzes immutable texture metadata and converts it into data in a more usable form. The {@link AnalyzedMetadata}
 * returned by the analyzer will later be given to the same plugin's {@link ComponentBuilder}.
 * @author soir20
 * @since 4.0.0
 */
@FunctionalInterface
public interface MetadataAnalyzer {

    /**
     * Converts the original metadata into a more usable form. <b>This method may be called from multiple
     * threads concurrently. If there is any state shared between calls, it must be synchronized properly
     * for concurrent usage.</b>
     * @param metadata      the original, immutable metadata. This metadata contains all metadata for
     *                      the texture, not just the metadata in this plugin's section name. The actual
     *                      metadata attributes themselves are stored within their section names. That is,
     *                      to access an attribute, a {@link MetadataView} for the section must be accessed
     *                      first.
     * @param imageWidth    width of the image associated with the metadata
     * @param imageHeight   height of the image associated with the metadata
     * @return an object with analyzed data
     * @throws InvalidMetadataException if the metadata is not valid
     */
    AnalyzedMetadata analyze(MetadataView metadata, int imageWidth, int imageHeight) throws InvalidMetadataException;

}
