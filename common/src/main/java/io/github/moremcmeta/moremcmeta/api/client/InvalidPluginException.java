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
 * Signals that one of the provided plugins is not valid for some reason.
 * @author soir20
 * @since 4.0.0
 */
public final class InvalidPluginException extends PluginException {

    /**
     * Creates a new exception with a detail message.
     * @param reason the reason the plugin is invalid
     */
    public InvalidPluginException(String reason) {
        super(reason);
    }

}
