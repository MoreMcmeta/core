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

package io.github.soir20.moremcmeta.fabric.impl.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

/**
 * Holds an event that is sent after the resource manager is initialized.
 * @author soir20
 */
public interface ResourceManagerInitializedCallback {
    Event<ManagerInitialized> EVENT = EventFactory.createArrayBacked(ManagerInitialized.class,
            (callbacks) -> (client) -> {
        for (ManagerInitialized callback : callbacks) {
            callback.onManagerInitialized(client);
        }
    });

    /**
     * A listener that is executed after the resource manager has been initialized.
     * @author soir20
     */
    @FunctionalInterface
    interface ManagerInitialized {

        /**
         * Called right after the resource manager has been initialized and
         * before any vanilla listeners have been registered.
         * @param client    the Minecraft client
         */
        void onManagerInitialized(Minecraft client);

    }

}
