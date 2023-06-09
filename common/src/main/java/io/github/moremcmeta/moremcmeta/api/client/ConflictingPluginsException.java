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

/**
 * Signals that at least two of the provided plugins conflict with each other.
 * @author soir20
 * @since 4.0.0
 */
public final class ConflictingPluginsException extends PluginException {

    /**
     * Creates a new exception with a detail message.
     * @param reason the reason the plugins are conflicting
     */
    public ConflictingPluginsException(String reason) {
        super(reason);
    }

}
