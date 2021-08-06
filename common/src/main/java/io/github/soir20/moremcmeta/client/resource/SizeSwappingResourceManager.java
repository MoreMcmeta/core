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
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final SimpleReloadableResourceManager ORIGINAL;
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

        ORIGINAL = requireNonNull(original, "Original resource manager cannot be null");
        RELOAD_CALLBACK = requireNonNull(reloadCallback, "Callback cannot be null");
    }

    /**
     * Adds a resource pack to the original manager.
     * @param packResources     pack to add
     */
    @Override
    public void add(PackResources packResources) {
        requireNonNull(packResources, "Pack resources cannot be null");
        ORIGINAL.add(packResources);
    }

    /**
     * Gets the original manager's namespaces.
     * @return the original manager's namespaces
     */
    @Override
    public Set<String> getNamespaces() {
        return ORIGINAL.getNamespaces();
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

        Resource resource = ORIGINAL.getResource(resourceLocation);
        ResourceLocation metadataLoc = getModMetadataLocation(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && ORIGINAL.hasResource(metadataLoc)) {
            Resource metadataResource = ORIGINAL.getResource(metadataLoc);

            if (metadataResource.getSourceName().equals(resource.getSourceName())) {
                InputStream metadataStream = metadataResource.getInputStream();
                resource = new SizeSwappingResource(resource, metadataStream);
            }
        }

        return resource;
    }

    /**
     * Determines whether the original manager has a resource.
     * @param resourceLocation      the location to check
     * @return whether the original manager has this resource
     */
    @Override
    public boolean hasResource(ResourceLocation resourceLocation) {
        requireNonNull(resourceLocation, "Location cannot be null");
        return ORIGINAL.hasResource(resourceLocation);
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

        List<Resource> resources = ORIGINAL.getResources(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && ORIGINAL.hasResource(metadataLoc)) {
            Map<String, Resource> modMetadataPacks = ORIGINAL.getResources(metadataLoc).stream()
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
     * Gets a list of resource locations from the original manager.
     * @param path          the path to look for resources in
     * @param predicate     test for file name
     * @return all resources in the original manager that meet the given criteria
     */
    @Override
    public Collection<ResourceLocation> listResources(String path, Predicate<String> predicate) {
        requireNonNull(path, "Path cannot be null");
        requireNonNull(predicate, "File name predicate cannot be null");
        return ORIGINAL.listResources(path, predicate);
    }

    /**
     * Closes the original manager.
     */
    @Override
    public void close() {
        ORIGINAL.close();
    }

    /**
     * Registers a reload listener. Listeners will execute with this wrapper as their parameter.
     * Listeners registered directly to the original manager (and not through this method) before
     * or after it was original will execute with the original manager as their parameter.
     * The original manager is aware of any listeners registered here.
     * @param preparableReloadListener      listener to add
     */
    @Override
    public void registerReloadListener(PreparableReloadListener preparableReloadListener) {
        requireNonNull(preparableReloadListener, "Reload listener cannot be null");
        
        ResourceManager thisManager = this;

        ORIGINAL.registerReloadListener(
                (preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) ->
                        preparableReloadListener.reload(
                                preparationBarrier, thisManager, profilerFiller,
                                profilerFiller2, executor, executor2
                        )
        );
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
        
        ReloadInstance reload = ORIGINAL.createReload(loadingExec, appExec, completableFuture, packs);
        reload.done().thenRun(RELOAD_CALLBACK);
        return reload;
    }

    /**
     * Gets all the packs in the original manager.
     * @return all the packs in the original manager
     */
    @Override
    public Stream<PackResources> listPacks() {
        return ORIGINAL.listPacks();
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
