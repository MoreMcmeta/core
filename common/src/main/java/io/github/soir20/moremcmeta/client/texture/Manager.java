/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.resources.ResourceLocation;

/**
 * A container for resources that can be added and removed. It represents what textures Minecraft is aware of.
 * @param <R> resource type
 * @author soir20
 */
public interface Manager<R> extends CustomTickable {

    /**
     * Prepares a texture and makes Minecraft aware of it.
     * @param location      file location of resource identical to how it is used in an entity/gui/map
     * @param resource      the actual resource that should be used
     */
    void register(ResourceLocation location, R resource);

    /**
     * Unregisters a resource so Minecraft is no longer aware of it.
     * This also allows the resource to be replaced.
     * @param location   file location of resource to delete
     */
    void unregister(ResourceLocation location);

    /**
     * Updates all animated resources that were loaded through this manager.
     */
    void tick();

}
