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

package io.github.soir20.moremcmeta.forge.api;

import io.github.soir20.moremcmeta.api.MoreMcmetaPlugin;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class MoreMcmetaPluginRegisterEvent extends Event implements IModBusEvent {
    private final Collection<MoreMcmetaPlugin> PLUGINS;

    public MoreMcmetaPluginRegisterEvent(Collection<MoreMcmetaPlugin> resultContainer) {
        PLUGINS = requireNonNull(resultContainer, "Queue cannot be null");
    }

    public void registerPlugin(MoreMcmetaPlugin plugin) {
        requireNonNull(plugin, "Plugin cannot be null");
        PLUGINS.add(plugin);
    }

}
