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

package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A container for resources.
 * @author soir20
 */
public interface ResourceCollection {

    /**
     * Gets a resource from this collection.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to get
     * @return the resource as a stream
     * @throws IOException if the resource does not exist
     */
    InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException;

    /**
     * Checks if this collection has a resource.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to search for
     * @return whether this collection contains the resource
     */
    boolean hasResource(PackType resourceType, ResourceLocation location);

    /**
     * Gets the namespaces of all resources in this collection.
     * @param resourceType      the type of resources to search
     * @return the unique namespaces of all resources in this collection
     */
    Set<String> getNamespaces(PackType resourceType);

}
