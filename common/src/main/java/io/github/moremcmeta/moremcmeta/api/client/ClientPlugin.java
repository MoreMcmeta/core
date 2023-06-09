/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
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

package io.github.moremcmeta.moremcmeta.api.client;

import static java.util.Objects.requireNonNull;

/**
 * <p>A user-provided plugin that interacts with the MoreMcmeta loader.</p>
 *
 * <p>MoreMcmeta avoids using `null`. Unless otherwise specified, neither parameters nor return values provided
 * by MoreMcmeta or by plugins should ever be `null`. In cases where the absence of a value is permissible,
 * {@link java.util.Optional} is used instead of `null`.</p>
 * @author soir20
 * @since 4.0.0
 */
public interface ClientPlugin {

    /**
     * Gets the unique ID for the plugin that will be used in logs. <b>This method may be called from
     * multiple threads concurrently. If there is any state shared between calls, it must be synchronized
     * properly for concurrent usage.</b>
     * @return plugin's display name
     */
    String id();

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
        super(requireNonNull(reason, "Plugin exception reason cannot be null"));
    }

}