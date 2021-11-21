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

package io.github.soir20.moremcmeta.client.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Swaps resources with {@link SizeSwappingResource}s if they have .moremcmeta metadata.
 * Otherwise, it performs identically to the {@link SimpleReloadableResourceManager} it
 * wraps. This extends {@link SimpleReloadableResourceManager} instead of implementing
 * {@link net.minecraft.server.packs.resources.ReloadableResourceManager} for compatibility
 * with other mods that expect a {@link SimpleReloadableResourceManager}.
 * @author soir20
 */
public class SizeSwappingResourceManager extends SimpleReloadableResourceManager {
    private static final String EXTENSION = ".moremcmeta";

    private final Runnable RELOAD_CALLBACK;

    /**
     * Creates a new size swapping resource manager wrapper.
     * @param original          original resource manager to wrap
     * @param reloadCallback    callback to run once all resource reloading has finished and
     *                          all listeners have executed
     */
    public SizeSwappingResourceManager(SimpleReloadableResourceManager original, Runnable reloadCallback) {

        // We only use the client-side resource manager
        super(PackType.CLIENT_RESOURCES);

        requireNonNull(original, "Original resource manager cannot be null");

        namespacedPacks.putAll(original.namespacedPacks);
        listeners.addAll(original.listeners);
        namespaces.addAll(original.namespaces);
        packs.addAll(original.packs);

        RELOAD_CALLBACK = requireNonNull(reloadCallback, "Callback cannot be null");
    }

    /**
     * Gets a resource from the original manager. The returned resource will be a
     * {@link SizeSwappingResource} if it has .moremcmeta metadata.
     * @param resourceLocation      location of the resource
     * @return the resource from the original manager, possibly a {@link SizeSwappingResource}
     * @throws IOException I/O error while retrieving the resource
     */
    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        requireNonNull(resourceLocation, "Location cannot be null");

        Resource resource = super.getResource(resourceLocation);
        ResourceLocation metadataLoc = getModMetadataLocation(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && hasResource(metadataLoc)) {
            Resource metadataResource = super.getResource(metadataLoc);

            if (metadataResource.getSourceName().equals(resource.getSourceName())) {
                InputStream metadataStream = metadataResource.getInputStream();
                resource = new SizeSwappingResource(resource, metadataStream);
            }
        }

        return resource;
    }

    /**
     * Gets all copies of resource from the original manager. The returned resources will be
     * {@link SizeSwappingResource}s if they have .moremcmeta metadata.
     * @param resourceLocation      location of the resources
     * @return the resources from the original manager, possibly {@link SizeSwappingResource}s
     * @throws IOException I/O error while retrieving the resources
     */
    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        requireNonNull(resourceLocation, "Location cannot be null");

        ResourceLocation metadataLoc = getModMetadataLocation(resourceLocation);

        List<Resource> resources = super.getResources(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && hasResource(metadataLoc)) {
            Map<String, Resource> modMetadataPacks = super.getResources(metadataLoc).stream()
                    .collect(Collectors.toMap(Resource::getSourceName, Function.identity()));

            Predicate<Resource> hasModMetadata = (resource) -> modMetadataPacks.containsKey(resource.getSourceName());
            Function<Resource, InputStream> getMetadataStream =
                    (resource) -> modMetadataPacks.get(resource.getSourceName()).getInputStream();

            resources = resources.stream().map((resource) -> hasModMetadata.test(resource) ?
                    new SizeSwappingResource(resource, getMetadataStream.apply(resource)) : resource
            ).collect(Collectors.toList());
        }

        return resources;
    }

    /**
     * Reloads the original resource manager, including any listeners that were registered
     * before or after it was wrapped.
     * @param loadingExec           executor for loading tasks
     * @param appExec               executor for application tasks
     * @param completableFuture     asynchronous reloading task
     * @param packs                 packs to add to listener
     * @return a reload instance for the current reload
     */
    @Override
    public ReloadInstance createReload(Executor loadingExec, Executor appExec,
                                       CompletableFuture<Unit> completableFuture, List<PackResources> packs) {
        requireNonNull(loadingExec, "Loading executor cannot be null");
        requireNonNull(appExec, "Application executor cannot be null");
        requireNonNull(completableFuture, "Completable future must not be null");
        requireNonNull(packs, "List of resource packs must not be null");
        
        ReloadInstance reload = super.createReload(loadingExec, appExec, completableFuture, packs);
        reload.done().thenRun(RELOAD_CALLBACK);
        return reload;
    }

    /**
     * Converts a location to its location with the metadata extension.
     * @param textureLocation       location to convert
     * @return that location with the metadata extension
     */
    private ResourceLocation getModMetadataLocation(ResourceLocation textureLocation) {
        String newPath = textureLocation.getPath() + EXTENSION;
        return new ResourceLocation(textureLocation.getNamespace(), newPath);
    }

}
