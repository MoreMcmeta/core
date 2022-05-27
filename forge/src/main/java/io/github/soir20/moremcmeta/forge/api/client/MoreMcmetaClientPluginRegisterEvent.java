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

package io.github.soir20.moremcmeta.forge.api.client;

import io.github.soir20.moremcmeta.api.client.MoreMcmetaClientPlugin;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Forge event to register plugins at the appropriate time. Dispatched on the mod event bus.
 * @author soir20
 * @since 4.0.0
 */
public class MoreMcmetaClientPluginRegisterEvent extends Event implements IModBusEvent {
    private final Collection<MoreMcmetaClientPlugin> PLUGINS;

    /**
     * Creates a new plugin registration event.
     * @param resultContainer       collection that will be updated with registered plugins
     */
    public MoreMcmetaClientPluginRegisterEvent(Collection<MoreMcmetaClientPlugin> resultContainer) {
        PLUGINS = requireNonNull(resultContainer, "Queue cannot be null");
    }

    /**
     * Registers a client plugin.
     * @param plugin        the plugin to register
     */
    public void registerPlugin(MoreMcmetaClientPlugin plugin) {
        requireNonNull(plugin, "Plugin cannot be null");
        PLUGINS.add(plugin);
    }

}
