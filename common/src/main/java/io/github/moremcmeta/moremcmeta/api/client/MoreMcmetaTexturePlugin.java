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
import io.github.moremcmeta.moremcmeta.api.client.texture.ComponentProvider;

/**
 * <p>A user-provided plugin that provides texture features using the MoreMcmeta loader.</p>
 *
 * <p>MoreMcmeta avoids using `null`. Unless otherwise specified, neither parameters nor return values provided
 * by MoreMcmeta or by plugins should ever be `null`. In cases where the absence of a value is permissible,
 * {@link java.util.Optional} is used instead of `null`.</p>
 * @author soir20
 * @since 4.0.0
 */
public interface MoreMcmetaTexturePlugin extends ClientPlugin {

    /**
     * Gets the section name the plugin is tied to. If the section name is present in a texture's metadata,
     * the plugin is applied to that texture. If the section name is not present, the plugin is not applied
     * to that texture. Two installed plugins may not have the same section name. <b>This method may be
     * called from multiple threads concurrently. If there is any state shared between calls, it must be
     * synchronized properly for concurrent usage.</b>
     * @return plugin's section name
     */
    String sectionName();

    /**
     * Gets the {@link MetadataParser} for this plugin. <b>This method may be called from multiple threads
     * concurrently. If there is any state shared between calls, it must be synchronized properly for
     * concurrent usage.</b>
     * @return plugin's metadata parser
     */
    MetadataParser parser();

    /**
     * Gets the {@link ComponentProvider} for this plugin. <b>This method may be called from multiple
     * threads concurrently. If there is any state shared between calls, it must be synchronized properly
     * for concurrent usage.</b>
     * @return plugin's component provider
     */
    ComponentProvider componentProvider();

}
