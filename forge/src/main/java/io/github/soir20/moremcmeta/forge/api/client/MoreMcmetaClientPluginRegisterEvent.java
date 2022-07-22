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

import io.github.soir20.moremcmeta.api.client.MoreMcmetaMetadataReaderPlugin;
import io.github.soir20.moremcmeta.api.client.MoreMcmetaTexturePlugin;
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
    private final Collection<MoreMcmetaTexturePlugin> TEXTURE_PLUGINS;
    private final Collection<MoreMcmetaMetadataReaderPlugin> READER_PLUGINS;

    /**
     * Creates a new plugin registration event.
     * @param textureResults       collection that will be updated with registered texture plugins
     * @param readerResults        collection that will be updated with registered reader plugins
     */
    public MoreMcmetaClientPluginRegisterEvent(Collection<MoreMcmetaTexturePlugin> textureResults,
                                               Collection<MoreMcmetaMetadataReaderPlugin> readerResults) {
        TEXTURE_PLUGINS = requireNonNull(textureResults, "Texture results cannot be null");
        READER_PLUGINS = requireNonNull(readerResults, "Reader results cannot be null");
    }

    /**
     * Registers a client texture plugin.
     * @param plugin        the plugin to register
     */
    public void register(MoreMcmetaTexturePlugin plugin) {
        requireNonNull(plugin, "Plugin cannot be null");
        TEXTURE_PLUGINS.add(plugin);
    }

    /**
     * Registers a client metadata reader plugin.
     * @param plugin        the plugin to register
     */
    public void register(MoreMcmetaMetadataReaderPlugin plugin) {
        requireNonNull(plugin, "Plugin cannot be null");
        READER_PLUGINS.add(plugin);
    }

}
