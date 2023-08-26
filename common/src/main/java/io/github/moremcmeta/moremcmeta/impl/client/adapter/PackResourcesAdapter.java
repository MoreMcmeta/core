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

package io.github.moremcmeta.moremcmeta.impl.client.adapter;

import io.github.moremcmeta.moremcmeta.impl.client.resource.ResourceCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Allows Minecraft's {@link PackResources} to implement {@link ResourceCollection}.
 * @author soir20
 */
public final class PackResourcesAdapter implements ResourceCollection {
    private final PackResources ORIGINAL;
    private final RootResourcesAdapter ROOT_RESOURCES;

    /**
     * Creates an adapter for resource packs.
     * @param original          the original pack to delegate to
     */
    public PackResourcesAdapter(PackResources original) {
        ORIGINAL = requireNonNull(original, "Original pack cannot be null");
        ROOT_RESOURCES = new RootResourcesAdapter(original);
    }

    /**
     * Gets a resource from this pack.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to get
     * @return the resource as a stream
     * @throws IOException if the resource does not exist
     */
    @Override
    public InputStream find(PackType resourceType, ResourceLocation location) throws IOException {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");

        if (RootResourcesAdapter.isRootResource(location)) {
            return ROOT_RESOURCES.find(resourceType, location);
        }

        IoSupplier<InputStream> resourceSupplier = ORIGINAL.getResource(resourceType, location);

        if (resourceSupplier == null) {
            throw new IOException(String.format("Could not find %s in pack type %s", location, resourceType));
        }

        return resourceSupplier.get();
    }

    /**
     * Checks if this pack has a resource.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to search for
     * @return whether this pack contains the resource
     */
    @Override
    public boolean contains(PackType resourceType, ResourceLocation location) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");
        return ROOT_RESOURCES.contains(resourceType, location)
                || (!RootResourcesAdapter.isRootResource(location) && ORIGINAL.getResource(resourceType, location) != null);
    }

    /**
     * Gets all the resource locations in this pack that match the provided filters.
     * @param resourceType      type of resources to look for
     * @param namespace         namespace of resources
     * @param pathStart         start of the path of the resources (not including the namespace)
     * @param fileFilter        filter for the file name
     * @return all the matching resource locations
     */
    @Override
    public Collection<ResourceLocation> list(PackType resourceType, String namespace, String pathStart,
                                             Predicate<String> fileFilter) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(fileFilter, "File filter cannot be null");

        if (RootResourcesAdapter.ROOT_NAMESPACE.equals(namespace)) {
            return ROOT_RESOURCES.list(resourceType, namespace, pathStart, fileFilter);
        }

        Set<ResourceLocation> resources = new HashSet<>();

        PackResources.ResourceOutput output = (location, resourceSupplier) -> {
            if (fileFilter.test(location.getPath())) {
                resources.add(location);
            }
        };

        ORIGINAL.listResources(resourceType, namespace, pathStart, output);

        return resources;
    }

    /**
     * Gets the namespaces of all resources in this pack.
     * @param resourceType      the type of resources to search
     * @return the unique namespaces of all resources in this pack
     */
    @Override
    public Set<String> namespaces(PackType resourceType) {
        requireNonNull(resourceType, "Resource type cannot be null");

        Set<String> namespaces = new HashSet<>(ORIGINAL.getNamespaces(resourceType));
        namespaces.addAll(ROOT_RESOURCES.namespaces(resourceType));

        return namespaces;
    }

    @Override
    public ResourceLocation locateRootResource(String rootResource) {
        requireNonNull(rootResource, "Root resource name cannot be null");
        return ROOT_RESOURCES.locateRootResource(rootResource);
    }

}
