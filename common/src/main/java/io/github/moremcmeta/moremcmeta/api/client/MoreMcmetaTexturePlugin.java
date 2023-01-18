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
 * A user-provided plugin that provides texture features using the MoreMcmeta loader.
 *
 * MoreMcmeta avoids using `null`. Unless otherwise specified, neither parameters nor return values provided
 * by MoreMcmeta or by plugins should ever be `null`. In cases where the absence of a value is permissible,
 * {@link java.util.Optional} is used instead of `null`.
 * @author soir20
 * @since 4.0.0
 */
public interface MoreMcmetaTexturePlugin extends ClientPlugin {

    /**
     * Gets the section name the plugin is tied to. If the section name is present in a texture's metadata,
     * the plugin is applied to that texture. If the section name is not present, the plugin is not applied
     * to that texture. Two installed plugins may not have the same section name. **This method may be
     * called from multiple threads concurrently. If there is any state shared between calls, it must be
     * synchronized properly for concurrent usage.**
     * @return plugin's section name
     */
    String sectionName();

    /**
     * Gets the {@link MetadataParser} for this plugin. **This method may be called from multiple threads
     * concurrently. If there is any state shared between calls, it must be synchronized properly for
     * concurrent usage.**
     * @return plugin's metadata parser
     */
    MetadataParser parser();

    /**
     * Gets the {@link ComponentProvider} for this plugin. **This method may be called from multiple
     * threads concurrently. If there is any state shared between calls, it must be synchronized properly
     * for concurrent usage.**
     * @return plugin's component provider
     */
    ComponentProvider componentProvider();

    /**
     * Indicates whether this plugin will be applied when a texture file and this plugin's metadata section
     * are in different resource packs. This method should always return the same boolean value; the plugin
     * must either always or never allow the texture and metadata section to be separated.
     *
     * **Most plugin authors should not override this method.** While allowing the metadata and texture file
     * to be in different packs may seem like a feature, enabling it may lead to strange behavior. For example,
     * say an animation plugin allows the metadata and texture to be in different packs. If a resource pack
     * overrides only the texture but not the animation metadata, the animation will apply to the overridden
     * texture. The animation will likely appear incorrect and odd to the user, and it may be difficult for
     * them to track down which resource pack is causing the issue. Enabling this feature is best used for
     * overlays or other types of metadata that are barely dependent on the texture file.
     * @return whether this plugin will be applied when a texture file and this plugin's metadata section
     *         are in different resource packs
     */
    default boolean allowTextureAndSectionInDifferentPacks() {
        return false;
    }

}
