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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Swaps resources with {@link SizeSwappingResource}s if they have .moremcmeta metadata.
 * Otherwise, it performs identically to the {@link SimpleReloadableResourceManager> it
 * wraps.}
 * @author soir20
 */
public class SizeSwappingResourceManager extends SimpleReloadableResourceManager {
    private static final String EXTENSION = ".moremcmeta";

    private final SimpleReloadableResourceManager ORIGINAL;

    /**
     * Creates a new size swapping resource manager wrapper.
     * @param original      original resource manager to wrap
     */
    public SizeSwappingResourceManager(SimpleReloadableResourceManager original) {

        // We only use the client-side resource manager
        super(PackType.CLIENT_RESOURCES);

        ORIGINAL = requireNonNull(original, "Original resource manager cannot be null");
    }

    /**
     * Adds a resource pack to the wrapped manager.
     * @param packResources     pack to add
     */
    @Override
    public void add(PackResources packResources) {
        requireNonNull(packResources, "Pack resources cannot be null");
        ORIGINAL.add(packResources);
    }

    /**
     * Gets the wrapped manager's namespaces.
     * @return the wrapped manager's namespaces
     */
    @Override
    public Set<String> getNamespaces() {
        return ORIGINAL.getNamespaces();
    }

    /**
     * Gets a resource from the wrapped manager. The returned resource will be a
     * {@link SizeSwappingResource} if it has .moremcmeta metadata.
     * @param resourceLocation      location of the resource
     * @return the resource from the wrapped manager, possibly a {@link SizeSwappingResource}
     * @throws IOException I/O error while retrieving the resource
     */
    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        requireNonNull(resourceLocation, "Location cannot be null");

        Resource resource = ORIGINAL.getResource(resourceLocation);
        ResourceLocation metadataLoc = getModMetadataLocation(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && ORIGINAL.hasResource(metadataLoc)) {
            InputStream metadataStream = ORIGINAL.getResource(metadataLoc).getInputStream();
            resource = new SizeSwappingResource(resource, metadataStream);
        }

        return resource;
    }

    /**
     * Determines whether the wrapped manager has a resource.
     * @param resourceLocation      the location to check
     * @return whether the wrapped manager has this resource
     */
    @Override
    public boolean hasResource(ResourceLocation resourceLocation) {
        requireNonNull(resourceLocation, "Location cannot be null");
        return ORIGINAL.hasResource(resourceLocation);
    }

    /**
     * Gets all copies of resource from the wrapped manager. The returned resources will be
     * {@link SizeSwappingResource}s if they have .moremcmeta metadata.
     * @param resourceLocation      location of the resources
     * @return the resources from the wrapped manager, possibly {@link SizeSwappingResource}s
     * @throws IOException I/O error while retrieving the resources
     */
    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        requireNonNull(resourceLocation, "Location cannot be null");

        ResourceLocation metadataLoc = getModMetadataLocation(resourceLocation);

        List<Resource> resources = ORIGINAL.getResources(resourceLocation);
        if (resourceLocation.getPath().endsWith(".png") && ORIGINAL.hasResource(metadataLoc)) {
            InputStream metadataStream = ORIGINAL.getResource(metadataLoc).getInputStream();
            resources = resources.stream().map(
                    (resource) -> new SizeSwappingResource(resource, metadataStream)
            ).collect(Collectors.toList());
        }

        return resources;
    }

    /**
     * Gets a list of resource locations from the wrapped manager.
     * @param path          the path to look for resources in
     * @param predicate     test for file name
     * @return all resources in the wrapped manager that meet the given criteria
     */
    @Override
    public Collection<ResourceLocation> listResources(String path, Predicate<String> predicate) {
        requireNonNull(path, "Path cannot be null");
        requireNonNull(predicate, "File name predicate cannot be null");
        return ORIGINAL.listResources(path, predicate);
    }

    /**
     * Closes the wrapped manager.
     */
    @Override
    public void close() {
        ORIGINAL.close();
    }

    /**
     * Registers a reload listener. Listeners will execute with this wrapper as their parameter.
     * Listeners registered directly to the wrapped manager (and not through this method) before
     * or after it was wrapped will execute with the wrapped manager as their parameter.
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
     * Reloads the wrapped resource manager
     * @param loadingExec           executor for loading tasks
     * @param appExec               executor for application tasks
     * @param completableFuture     asynchronous reloading task
     * @param packs                 packs to add to listener
     * @return a reload instance for the current reload
     */
    @Override
    public ReloadInstance createFullReload(Executor loadingExec, Executor appExec,
                                           CompletableFuture<Unit> completableFuture, List<PackResources> packs) {
        requireNonNull(loadingExec, "Loading executor cannot be null");
        requireNonNull(appExec, "Application executor cannot be null");
        requireNonNull(completableFuture, "Completable future must not be null");
        requireNonNull(packs, "List of resource packs must not be null");
        
        return ORIGINAL.createFullReload(loadingExec, appExec, completableFuture, packs);
    }

    /**
     * Gets all the packs in the wrapped manager.
     * @return all the packs in the wrapped manager
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
