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
import net.minecraft.server.packs.resources.IoSupplier;

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

    /**
     * Pseudo-namespace that all locations returned by {@link PackResourcesAdapter#locateRootResource(String)}
     * will have. Some mods scan all resources inside a resource pack, producing {@link StackOverflowError}s
     * in infinite recursion with the
     * {@link io.github.moremcmeta.moremcmeta.impl.client.resource.SpriteFrameSizeFixPack}. A unique
     * pseudo-namespace sidesteps this issue because those resource packs probably need to use their own unique
     * namespace to avoid an infinite recursion in Minecraft's resource retrieval methods. This means the
     * {@link io.github.moremcmeta.moremcmeta.impl.client.resource.OrderedResourceRepository} in the sprite
     * fix pack won't check the other mod's pack for most resources.
     */
    public static final String ROOT_NAMESPACE = "__moremcmeta_root__";

    private final PackResources ORIGINAL;
    private final Map<ResourceLocation, IoSupplier<InputStream>> ROOT_RESOURCES;
    private final String ROOT_PATH_PREFIX;

    /**
     * Converts a root resource location returned from a {@link RootResourcesAdapter} to one compatible
     * with the resource pack screen.
     * @param location      location to convert
     * @return location for the pack screen, or the original location if not a root location
     */
    public static ResourceLocation locateForPackScreen(ResourceLocation location) {
        requireNonNull(location, "Location cannot be null");

        if (location.getNamespace().equals(ROOT_NAMESPACE)) {
            return new ResourceLocation("minecraft", location.getPath());
        }

        return location;
    }

    /**
     * Creates a new adapter for root pack resources.
     * @param original      original resource pack to wrap
     */
    public RootResourcesAdapter(PackResources original) {
        ORIGINAL = requireNonNull(original, "Original pack cannot be null");
        ROOT_RESOURCES = new ConcurrentHashMap<>();

        String packId = ORIGINAL.packId();
        String sanitizedName = Util.sanitizeName(packId, ResourceLocation::validPathChar);

        @SuppressWarnings("deprecation")
        String idHash = Hashing.sha1().hashUnencodedChars(packId).toString();

        // Must be the same prefix as generated in PackSelectionScreen#loadPackIcon()
        ROOT_PATH_PREFIX = "pack/" + sanitizedName + "/" + idHash + "/";
    }

    @Override
    public InputStream find(PackType resourceType, ResourceLocation location) throws IOException {
        requireNonNull(resourceType, "Resource type cannot be null");
        requireNonNull(location, "Location cannot be null");

        IoSupplier<InputStream> resourceSupplier = ROOT_RESOURCES.get(location);

        if (resourceSupplier == null) {
            throw new IOException(String.format("Could not find %s in pack type %s", location, resourceType));
        }

        return resourceSupplier.get();
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
        return ImmutableSet.of(ROOT_NAMESPACE);
    }

    @Override
    public ResourceLocation locateRootResource(String rootResource) {
        requireNonNull(rootResource, "Root resource name cannot be null");
        String fileName = rootResource.replaceAll("^pack.png", "icon");

        // Must be the same ResourceLocation as generated in PackSelectionScreen#loadPackIcon()
        ResourceLocation location = new ResourceLocation(ROOT_NAMESPACE, ROOT_PATH_PREFIX + fileName);

        ROOT_RESOURCES.computeIfAbsent(location, (loc) -> ORIGINAL.getRootResource(rootResource));

        return location;
    }
}
