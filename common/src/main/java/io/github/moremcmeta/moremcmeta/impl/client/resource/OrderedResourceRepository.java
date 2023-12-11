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

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Holds several {@link ResourceCollection}s and searches them in the provided order.
 * @author soir20
 */
public class OrderedResourceRepository {
    private final PackType RESOURCE_TYPE;
    private final ImmutableList<ResourceCollection> COLLECTIONS;

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
    public PackType resourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * Gets all collections in this repository, in order.
     * @return all collections in this repository
     */
    public List<ResourceCollection> collections() {
        return COLLECTIONS;
    }

    /**
     * Gets the first collection that has a given resource.
     * @param location          location of the resource to search for
     * @return the collection with the given resource
     * @throws IOException if the resource is not found in any collection
     */
    public ResourceCollectionResult firstCollectionWith(ResourceLocation location) throws IOException {
        requireNonNull(location, "Location cannot be null");

        Optional<ResourceCollectionResult> collectionWithResource = collectionsByNamespace(location.getNamespace())
                .stream()
                .filter((collectionResult) -> collectionResult.collection().contains(RESOURCE_TYPE, location))
                .findFirst();

        if (!collectionWithResource.isPresent()) {
            throw new IOException("Resource not found in any collection: " + location);
        }

        return collectionWithResource.get();
    }

    /**
     * Checks if any collection has the given resource.
     * @param location      the resource to check for
     * @return whether this repository has that resource
     */
    public boolean contains(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");

        return collectionsByNamespace(location.getNamespace())
                .stream()
                .anyMatch((collectionResult) -> collectionResult.collection().contains(RESOURCE_TYPE, location));
    }

    /**
     * Lists all resources in any collection that match the provided filters.
     * @param pathStart     the required start of each resource's path
     * @param fileFilter    filter for the file name
     * @return all matching resource locations
     */
    public Set<ResourceLocation> list(String pathStart, Predicate<String> fileFilter) {
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(fileFilter, "Path filter cannot be null");

        return COLLECTIONS.stream().flatMap(
                (collection) -> collection.namespaces(RESOURCE_TYPE).stream().flatMap(
                        (namespace) -> collection.list(RESOURCE_TYPE, namespace, pathStart, fileFilter).stream()
                )
        ).collect(Collectors.toSet());
    }

    /**
     * Gets all collections that contain resources for the given namespace.
     * @param namespace     namespace to check
     * @return all collections that have resources in the given namespace
     */
    private List<ResourceCollectionResult> collectionsByNamespace(String namespace) {

        // Filter collections when resources requested, rather than constructor, avoids inf. recursion with other mods
        return IntStream.range(0, COLLECTIONS.size())
                .mapToObj((index) -> new ResourceCollectionResult(COLLECTIONS.get(index), index))
                .filter((collection) -> collection.collection().namespaces(RESOURCE_TYPE).contains(namespace))
                .collect(Collectors.toList());

    }

    /**
     * Contains the result of a {@link ResourceCollection} search.
     * @author soir20
     */
    public final static class ResourceCollectionResult {
        private final ResourceCollection COLLECTION;
        private final int INDEX;

        /**
         * Creates a new collection result.
         * @param collection        collection that was found
         * @param index             index of the collection (where lower indices are higher packs)
         */
        private ResourceCollectionResult(ResourceCollection collection, int index) {
            COLLECTION = requireNonNull(collection, "Resource collection cannot be null");
            INDEX = index;
        }

        /**
         * Gets the collection from this result.
         * @return collection that was found
         */
        public ResourceCollection collection() {
            return COLLECTION;
        }

        /**
         * Gets the index of the collection that was found (lower indices are higher packs).
         * @return index of the collection that was found
         */
        public int collectionIndex() {
            return INDEX;
        }

    }

}
