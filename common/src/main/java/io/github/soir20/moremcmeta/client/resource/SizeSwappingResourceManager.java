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

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.RGBAImageFrame;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
public class SizeSwappingResourceManager implements ResourceManager {
    private static final String EXTENSION = ".moremcmeta";

    private final ResourceManager ORIGINAL;
    private final Map<ResourceLocation, EventDrivenTexture.Builder> TEXTURES;

    /**
     * Creates a new size swapping resource manager wrapper.
     * @param original          original resource manager to wrap
     */
    public SizeSwappingResourceManager(ResourceManager original,
                                       Map<ResourceLocation, EventDrivenTexture.Builder> textures) {
        ORIGINAL = requireNonNull(original, "Original resource manager cannot be null");
        TEXTURES = requireNonNull(textures, "Textures cannot be null");
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

        System.out.println(resourceLocation);

        Resource originalResource = ORIGINAL.getResource(resourceLocation);
        if (!TEXTURES.containsKey(resourceLocation)) {
            return originalResource;
        }

        Pair<Integer, Integer> frameSize = getFrameSize(resourceLocation);
        int frameWidth = frameSize.getFirst();
        int frameHeight = frameSize.getSecond();

        return new SizeSwappingResource(originalResource, frameWidth, frameHeight);
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
        if (TEXTURES.containsKey(resourceLocation)) {
            Pair<Integer, Integer> frameSize = getFrameSize(resourceLocation);
            int frameWidth = frameSize.getFirst();
            int frameHeight = frameSize.getSecond();

            Set<String> modMetadataPacks = ORIGINAL.getResources(metadataLoc).stream().map(Resource::getSourceName)
                    .collect(Collectors.toSet());
            Predicate<Resource> hasModMetadata = (resource) -> modMetadataPacks.contains(resource.getSourceName());

            resources = resources.stream().map((resource) -> hasModMetadata.test(resource) ?
                    new SizeSwappingResource(resource, frameWidth, frameHeight) : resource
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
     * Gets all the packs in the original manager.
     * @return all the packs in the original manager
     */
    @Override
    public Stream<PackResources> listPacks() {
        return ORIGINAL.listPacks();
    }

    /**
     * Gets the frame size of a texture, assuming it is an animated texture.
     * @param location      the resource location of the texture (non-sprite location)
     * @return a (frameWidth, frameHeight) tuple
     */
    private Pair<Integer, Integer> getFrameSize(ResourceLocation location) {
        Optional<RGBAImageFrame> initialFrame = TEXTURES.get(location).getImage();
        if (initialFrame.isEmpty()) {
            throw new IllegalStateException("Missing initial image guaranteed by texture reader");
        }

        int frameWidth = initialFrame.get().getWidth();
        int frameHeight = initialFrame.get().getHeight();

        return Pair.of(frameWidth, frameHeight);
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
