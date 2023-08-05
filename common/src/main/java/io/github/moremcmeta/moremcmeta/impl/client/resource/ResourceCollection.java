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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

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
    InputStream find(PackType resourceType, ResourceLocation location) throws IOException;

    /**
     * Checks if this collection has a resource.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to search for
     * @return whether this collection contains the resource
     */
    boolean contains(PackType resourceType, ResourceLocation location);

    /**
     * Gets all the resource locations in this collection that match the provided filters.
     * @param resourceType      type of resources to look for
     * @param namespace         namespace of resources
     * @param pathStart         start of the path of the resources (not including the namespace)
     * @param fileFilter        filter for the file name
     * @return all the matching resource locations
     */
    Collection<ResourceLocation> list(PackType resourceType, String namespace, String pathStart,
                                      Predicate<String> fileFilter);

    /**
     * Gets the namespaces of all resources in this collection.
     * @param resourceType      the type of resources to search
     * @return the unique namespaces of all resources in this collection
     */
    Set<String> namespaces(PackType resourceType);

    /**
     * Finds the {@link ResourceLocation} that can be used to retrieve the given resource at the
     * root of this collection. This method returns a location regardless of whether the resource is
     * present in the collection.
     * @param rootResource      resource at the root of this collection
     * @return the full location where the given root resource is located in this collection
     */
    ResourceLocation locateRootResource(String rootResource);

}
