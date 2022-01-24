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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Holds several {@link ResourceCollection}s and searches them in the provided order.
 * @author soir20
 */
public class OrderedResourceRepository {
    private final PackType RESOURCE_TYPE;
    private final ImmutableCollection<ResourceCollection> COLLECTIONS;

    /**
     * Creates a new ordered group of {@link ResourceCollection}s.
     * @param resourceType              type of resources to search
     * @param resourceCollections       resource collections in the order they will be searched
     */
    public OrderedResourceRepository(PackType resourceType,
                                     Collection<? extends ResourceCollection> resourceCollections) {
        RESOURCE_TYPE = requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(resourceCollections, "Resource collection list cannot be null");
        if (resourceCollections.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("Individual resource collections cannot be null");
        }

        COLLECTIONS = ImmutableList.copyOf(resourceCollections);
    }

    /**
     * Gets the type of resources in this repository (client or server).
     * @return the type of resources in this repository
     */
    public PackType getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * Gets the first collection that has a given resource.
     * @param location          location of the resource to search for
     * @return the collection with the given resource
     * @throws IOException if the resource is not found in any collection
     */
    public ResourceCollection getFirstCollectionWith(ResourceLocation location) throws IOException {
        requireNonNull(location, "Location cannot be null");

        Optional<ResourceCollection> collectionWithResource = COLLECTIONS.stream().filter((collection) ->
                collection.hasResource(RESOURCE_TYPE, location)
        ).findFirst();

        if (collectionWithResource.isEmpty()) {
            throw new IOException("Resource not found in any collection: " + location);
        }

        return collectionWithResource.get();
    }

    /**
     * Checks if any collection has the given resource.
     * @param location      the resource to check for
     * @return whether this repository has that resource
     */
    public boolean hasResource(ResourceLocation location) {
        return COLLECTIONS.stream().anyMatch((collection) ->
                collection.hasResource(RESOURCE_TYPE, location)
        );
    }

    /**
     * Lists all resources in any collection that match the provided filters.
     * @param pathStart     the required start of each resource's path
     * @param fileFilter    filter for the file name
     * @return all matching resource locations
     */
    public Set<ResourceLocation> listResources(String pathStart, Predicate<String> fileFilter) {
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(fileFilter, "Path filter cannot be null");

        return COLLECTIONS.stream().flatMap(
                (collection) -> collection.getNamespaces(RESOURCE_TYPE).stream().flatMap(
                        (namespace) -> collection.getResources(RESOURCE_TYPE, namespace, pathStart, fileFilter).stream()
                )
        ).collect(Collectors.toSet());
    }

}
