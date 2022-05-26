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

public interface MoreMcmetaPlugin {

    String displayName();

    String sectionName();

    MetadataParser parser();

    ComponentProvider componentProvider();

    /**
     * Signals that one of the provided plugins is not valid for some reason.
     * @author soir20
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