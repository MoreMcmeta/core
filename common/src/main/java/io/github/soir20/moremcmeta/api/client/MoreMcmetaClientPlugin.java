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

package io.github.soir20.moremcmeta.api.client;

import io.github.soir20.moremcmeta.api.client.metadata.MetadataParser;
import io.github.soir20.moremcmeta.api.client.texture.ComponentProvider;

/**
 * A user-provided plugin that interacts with the MoreMcmeta loader.
 * @author soir20
 * @since 4.0.0
 */
public interface MoreMcmetaClientPlugin {

    /**
     * Gets the display name for the plugin that will be used in logs. **This method may be called from
     * multiple threads concurrently. If there is any state shared between calls, it must be synchronized
     * properly for concurrent usage.**
     * @return plugin's display name
     */
    String displayName();

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
     * Signals that one of the provided plugins is not valid for some reason.
     * @author soir20
     * @since 4.0.0
     */
    final class IncompletePluginException extends PluginException {

        /**
         * Creates a new exception with a detail message.
         * @param reason    the reason the plugin is invalid
         */
        public IncompletePluginException(String reason) {
            super(reason);
        }

    }

    /**
     * Signals that at least two of the provided plugins conflict with each other.
     * @author soir20
     * @since 4.0.0
     */
    final class ConflictingPluginsException extends PluginException {

        /**
         * Creates a new exception with a detail message.
         * @param reason    the reason the plugins are conflicting
         */
        public ConflictingPluginsException(String reason) {
            super(reason);
        }

    }

}

/**
 * Signals that there is some issue with the registered plugins.
 * @author soir20
 */
class PluginException extends RuntimeException {

    /**
     * Creates a new exception with a detail message.
     * @param reason    the reason the plugins are not valid
     */
    public PluginException(String reason) {
        super(reason);
    }

}