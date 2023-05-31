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

package io.github.moremcmeta.moremcmeta.api.client;

import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;

/**
 * <p>A user-provided plugin that reads custom metadata formats using the MoreMcmeta loader.</p>
 *
 * <p>MoreMcmeta avoids using `null`. Unless otherwise specified, neither parameters nor return values provided
 * by MoreMcmeta or by plugins should ever be `null`. In cases where the absence of a value is permissible,
 * {@link java.util.Optional} is used instead of `null`.</p>
 * @author soir20
 * @since 4.0.0
 */
public interface MoreMcmetaMetadataParserPlugin extends ClientPlugin {

    /**
     * Gets the file extension the plugin is tied to (without a period). If a metadata file has this extension,
     * the plugin will read that metadata. If the extension is not present, the plugin is not applied
     * to that metadata. Two installed plugins may not have the same extension. <b>This method may be
     * called from multiple threads concurrently. If there is any state shared between calls, it must be
     * synchronized properly for concurrent usage.</b>
     * @return plugin's section name
     */
    String extension();

    /**
     * Gets the {@link MetadataParser} for this plugin. <b>This method may be called from multiple threads
     * concurrently. If there is any state shared between calls, it must be synchronized properly for
     * concurrent usage.</b>
     * @return plugin's metadata reader
     */
    MetadataParser metadataParser();

}
