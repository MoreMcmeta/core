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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.client.io.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Provides dummy metadata for mod-animated atlas sprites when vanilla metadata is requested.
 * This prevents the atlas sprites from being "squished" because only the first frame is stitched
 * onto the atlas, not all of them.
 * @author soir20
 */
public class SpriteFrameSizeFixPack implements PackResources {
    private static final String VANILLA_METADATA_EXTENSION = ".mcmeta";
    private final ImmutableMap<? extends ResourceLocation, ? extends TextureData<?>> TEXTURES;

    private final List<ResourceCollection> OTHER_PACKS;

    /**
     * Creates a new sprite fix pack.
     * @param textures              textures controlled by the mod. Every texture must have an image set.
     * @param resourceRepository    repository of all resources to search, excluding this pack
     */
    public SpriteFrameSizeFixPack(Map<? extends ResourceLocation, ? extends TextureData<?>> textures,
                                  OrderedResourceRepository resourceRepository) {
        requireNonNull(textures, "Textures cannot be null");

        List<ResourceCollection> reversedList = new ArrayList<>(otherPacks);
        Collections.reverse(reversedList);
        OTHER_PACKS = ImmutableList.copyOf(reversedList);

        TEXTURES = ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a resource at the root of this pack.
     * @param resourceName      name of resource
     * @return the resource with that name or null if not found
     */
    @Nullable
    @Override
    public InputStream getRootResource(String resourceName) {
        requireNonNull(resourceName, "Resource name cannot be null");
        return null;
    }

    /**
     * Gets a resource from this pack. Only mod-controlled textures provided in
     * the constructor can be found. .mcmeta files for those textures are replaced
     * with dummy metadata.
     * @param packType      client or server resources. Only client resources are available.
     * @param location      location of the resource to retrieve
     * @return the requested resource
     * @throws IOException if the requested resource is not found or a server pack type is provided
     */
    @Override
    public InputStream getResource(PackType packType, ResourceLocation location) throws IOException {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(location, "Location cannot be null");

        if (packType != PackType.CLIENT_RESOURCES) {
            throw new IOException("MoreMcmeta's internal pack only contains client resources");
        }

        ResourceLocation textureLocation = getTextureLocation(location);
        boolean isKnownTexture = TEXTURES.containsKey(textureLocation);
        boolean isVanillaMetadata = location.getPath().endsWith(VANILLA_METADATA_EXTENSION);

        if (isKnownTexture && isVanillaMetadata) {
            TextureData<?> textureData = TEXTURES.get(textureLocation);
            int frameWidth = textureData.getFrameWidth();
            int frameHeight = textureData.getFrameHeight();
            return new ByteArrayInputStream(
                    makeEmptyAnimationJson(frameWidth, frameHeight).getBytes(StandardCharsets.UTF_8)
            );
        } else if (!isKnownTexture) {
            throw new IOException("Requested non-animated resource from MoreMcmeta's internal pack");
        }

        return packWithResource.get().getResource(packType, textureLocation);
    }

    /**
     * Gets the locations of the available resources in this pack (the textures provided in
     * the constructor).
     * @param packType          client or server resources. Only client resources are available.
     * @param namespace         namespace of the requested resources
     * @param pathStart         required start of the resources' paths (like folders)
     * @param depth             maximum depth of directory tree to search
     * @param pathFilter        filter for entire paths, including the file name and extension
     * @return the matching resources in this pack
     */
    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart,
                                                     int depth, Predicate<String> pathFilter) {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(pathFilter, "Path filter cannot be null");

        if (depth < 0) {
            throw new IllegalArgumentException("Depth is negative");
        }

        // Vanilla packs exclude .mcmeta metadata files, so we should not include them here
        return TEXTURES.keySet().stream().filter((location) -> {
            String path = location.getPath();
            boolean isRightNamespace = location.getNamespace().equals(namespace);
            boolean isRightPath = path.startsWith(pathStart + "/") && pathFilter.test(path);
            boolean isRightDepth = StringUtils.countMatches(path, '/') <= depth;

            return isRightNamespace && isRightPath && isRightDepth;
        }).collect(Collectors.toList());

    }

    /**
     * Checks if this pack contains a resource. Only contains textures and their metadata for
     * the textures provided in the constructor.
     * @param packType      client or server resources. Only client resources are available.
     * @param location      location of the resource to look for
     * @return whether the pack contains the requested resource
     */
    @Override
    public boolean hasResource(PackType packType, ResourceLocation location) {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(location, "Location cannot be null");

        if (packType != PackType.CLIENT_RESOURCES) {
            return false;
        }

        return TEXTURES.containsKey(getTextureLocation(location));
    }

    /**
     * Gets the unique namespaces of all the resources in this pack.
     * @param packType      client or server resources. Only client resources are available.
     * @return the unique namespaces of this pack's resources
     */
    @Override
    public Set<String> getNamespaces(PackType packType) {
        requireNonNull(packType, "Pack type cannot be null");

        if (packType != PackType.CLIENT_RESOURCES) {
            return Set.of();
        }

        return OTHER_PACKS.stream().flatMap(
                (pack) -> pack.getNamespaces(packType).stream()
        ).collect(Collectors.toSet());
    }

    /**
     * Gets the metadata for this pack (not individual resource metadata).
     * @param metadataSectionSerializer     metadata serializer to read metadata
     * @param <T>                           metadata section type
     * @return the requested section of metadata or null if there is none
     */
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
        requireNonNull(metadataSectionSerializer, "Serializer cannot be null");
        return null;
    }

    /**
     * Gets the name of this resource pack.
     * @return the name of this resource pack
     */
    @Override
    public String getName() {
        return "__MoreMcmeta Internal__";
    }

    /**
     * Closes this resource pack and any internal resources it has open.
     */
    @Override
    public void close() {}

    /**
     * Converts a location ending with the vanilla metadata extension (.mcmeta) to the
     * texture location.
     * @param location      location of vanilla metadata for a texture
     * @return location of the texture associated with the metadata or the same location
     *         if it is not the location of vanilla metadata
     */
    private ResourceLocation getTextureLocation(ResourceLocation location) {
        return new ResourceLocation(
                location.getNamespace(), location.getPath().replace(VANILLA_METADATA_EXTENSION, "")
        );
    }

    /**
     * Check if a resource pack has the given resource more efficiently than directly using the
     * {@link ResourceCollection#hasResource(PackType, ResourceLocation)} method.
     * @param otherPack     the other pack to search
     * @param location      the location of the resource to look for
     * @return whether the given pack is not the same as this pack and has the requested resource
     */
    private boolean checkResource(ResourceCollection otherPack, ResourceLocation location) {
        return otherPack.getNamespaces(PackType.CLIENT_RESOURCES).contains(location.getNamespace())
                && otherPack.hasResource(PackType.CLIENT_RESOURCES, location);
    }

    /**
     * Makes dummy metadata for an animation with one frame of the given size.
     * @param frameWidth        width of a frame
     * @param frameHeight       height of a frame
     * @return dummy metadata with the given frame size
     */
    private String makeEmptyAnimationJson(int frameWidth, int frameHeight) {
        return String.format(
                "{" +
                        "\"animation\": {" +
                        "\"width\": %d," +
                        "\"height\": %d," +
                        "\"frames\": [0]" +
                        "}" +
                        "}",
                frameWidth, frameHeight
        );
    }

}