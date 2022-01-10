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
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
     * Gets the given resources from the first collection that contains all of them.
     * @param locations     the resources to retrieve
     * @return the requested resources from the same collection
     * @throws IOException if the resources are not found together in any collection
     */
    public FoundResources getFromSameCollection(ResourceLocation... locations) throws IOException {
        requireNonNull(locations, "Resource locations cannot be null");
        if (Arrays.stream(locations).anyMatch(Objects::isNull)) {
            throw new NullPointerException("Individual resource locations cannot be null");
        }

        Optional<ResourceCollection> collectionWithResource = COLLECTIONS.stream().filter((collection) ->
                Arrays.stream(locations).allMatch((location) -> collection.hasResource(RESOURCE_TYPE, location))
        ).findFirst();

        if (collectionWithResource.isEmpty()) {
            throw new IOException("Resources not found in same collection: " + Arrays.toString(locations));
        }

        ResourceCollection collection = collectionWithResource.get();

        Map<ResourceLocation, InputStream> streams = new HashMap<>();
        for (ResourceLocation location : locations) {
            streams.put(location, collection.getResource(RESOURCE_TYPE, location));
        }
        return new FoundResources(streams);
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

    /**
     * Gets all the unique namespaces for the resources in this repository.
     * @return all unique namespaces in the repository
     */
    public Set<String> getNamespaces() {
        return COLLECTIONS.stream().flatMap(
                (collection) -> collection.getNamespaces(RESOURCE_TYPE).stream()
        ).collect(Collectors.toSet());
    }

    /**
     * Container for resources returned from a {@link OrderedResourceRepository}. All requested
     * resources are guaranteed to exist.
     * @author soir20
     */
    public static class FoundResources implements Closeable {
        private final ImmutableMap<ResourceLocation, InputStream> RESOURCES;

        /**
         * Creates a new container for retrieved resources.
         * @param resources     the resources retrieved
         */
        protected FoundResources(Map<ResourceLocation, InputStream> resources) {
            requireNonNull(resources, "Resources cannot be null");
            RESOURCES = ImmutableMap.copyOf(resources);
        }

        /**
         * Gets a resource at the given location.
         * @param location      location of the resource to retrieve. Must have been requested from the
         *                      {@link OrderedResourceRepository}.
         * @return the resource at the given location
         */
        public InputStream get(ResourceLocation location) {
            if (!RESOURCES.containsKey(location)) {
                throw new IllegalArgumentException("Tried to retrieve resource that was not originally requested");
            }

            return RESOURCES.get(location);
        }

        /**
         * Closes all the resources in this container. If any resource was already closed,
         * it is skipped, and there is no exception.
         * @throws IOException if there is a problem closing a resource
         */
        @Override
        public void close() throws IOException {
            for (InputStream stream : RESOURCES.values()) {
                stream.close();
            }
        }

    }
}
