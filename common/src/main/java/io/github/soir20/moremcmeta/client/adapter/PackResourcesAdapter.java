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

package io.github.soir20.moremcmeta.client.adapter;

import io.github.soir20.moremcmeta.client.resource.ResourceCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Allows Minecraft's {@link PackResources} to implement {@link ResourceCollection}.
 * @author soir20
 */
public class PackResourcesAdapter implements ResourceCollection {
    private final PackResources ORIGINAL;

    /**
     * Creates an adapter for resource packs.
     * @param original      the original pack to delegate to
     */
    public PackResourcesAdapter(PackResources original) {
        ORIGINAL = requireNonNull(original, "Original pack cannot be null");
    }

    /**
     * Gets a resource from this pack.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to get
     * @return the resource as a stream
     * @throws IOException if the resource does not exist
     */
    @Override
    public InputStream getResource(PackType resourceType, ResourceLocation location) throws IOException {
        return ORIGINAL.getResource(resourceType, location);
    }

    /**
     * Checks if this pack has a resource.
     * @param resourceType      the type of resources to search
     * @param location          the location of the resource to search for
     * @return whether this pack contains the resource
     */
    @Override
    public boolean hasResource(PackType resourceType, ResourceLocation location) {
        return ORIGINAL.hasResource(resourceType, location);
    }

    /**
     * Gets the namespaces of all resources in this pack.
     * @param resourceType      the type of resources to search
     * @return the unique namespaces of all resources in this pack
     */
    @Override
    public Set<String> getNamespaces(PackType resourceType) {
        return ORIGINAL.getNamespaces(resourceType);
    }

}
