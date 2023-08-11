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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import io.github.moremcmeta.moremcmeta.impl.client.resource.ResourceCollection;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ResourceCollection} that only contains the resources at the root of a resource pack.
 * @author soir20
 */
public final class RootResourcesAdapter implements ResourceCollection {
    private final PackResources ORIGINAL;
    private final Map<ResourceLocation, String> ROOT_RESOURCES;
    private final String ROOT_PATH_PREFIX;

    /**
     * Creates a new adapter for root pack resources.
     * @param original      original resource pack to wrap
     * @param packId        ID of the resource pack
     */
    public RootResourcesAdapter(PackResources original, String packId) {
        ORIGINAL = requireNonNull(original, "Original pack cannot be null");
        requireNonNull(packId, "Pack ID cannot be null");
        ROOT_RESOURCES = new ConcurrentHashMap<>();

        String sanitizedName = Util.sanitizeName(packId, ResourceLocation::validPathChar);

        @SuppressWarnings({"deprecation", "UnstableApiUsage"})
        String idHash = Hashing.sha1().hashUnencodedChars(packId).toString();

        // Must be the same prefix as generated in PackSelectionScreen#loadPackIcon()
        ROOT_PATH_PREFIX = "pack/" + sanitizedName + "/" + idHash + "/";
    }

    @Override
    public InputStream find(PackType resourceType, ResourceLocation location) throws IOException {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");

        if (!ROOT_RESOURCES.containsKey(location)) {
            throw new IOException(String.format("Could not find %s in pack type %s", location, resourceType));
        }

        return ORIGINAL.getRootResource(ROOT_RESOURCES.get(location));
    }

    @Override
    public boolean contains(PackType resourceType, ResourceLocation location) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");
        return ROOT_RESOURCES.containsKey(location);
    }

    @Override
    public Collection<ResourceLocation> list(PackType resourceType, String namespace, String pathStart, Predicate<String> fileFilter) {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(fileFilter, "File filter cannot be null");
        return ImmutableList.of();
    }

    @Override
    public Set<String> namespaces(PackType resourceType) {
        requireNonNull(resourceType, "Resource type cannot be null");
        return ImmutableSet.of();
    }

    @Override
    public ResourceLocation locateRootResource(String rootResource) {
        requireNonNull(rootResource, "Root resource name cannot be null");
        String fileName = rootResource.replaceAll("^pack.png", "icon");

        // Must be the same ResourceLocation as generated in PackSelectionScreen#loadPackIcon()
        ResourceLocation location = new ResourceLocation(ROOT_PATH_PREFIX + fileName);

        try {
            if (ORIGINAL.getRootResource(rootResource) != null) {
                ROOT_RESOURCES.putIfAbsent(location, rootResource);
            }
        } catch (IOException ignored) {}

        return location;
    }
}
