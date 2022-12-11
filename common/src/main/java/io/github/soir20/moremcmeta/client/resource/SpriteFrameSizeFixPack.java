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

package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.client.io.TextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
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

    private final OrderedResourceRepository RESOURCE_REPOSITORY;

    /**
     * Creates a new sprite fix pack.
     * @param textures              textures controlled by the mod. Every texture must have an image set.
     * @param resourceRepository    repository of all resources to search, excluding this pack
     */
    public SpriteFrameSizeFixPack(Map<? extends ResourceLocation, ? extends TextureData<?>> textures,
                                  OrderedResourceRepository resourceRepository) {
        requireNonNull(textures, "Textures cannot be null");
        RESOURCE_REPOSITORY = requireNonNull(resourceRepository, "Packs cannot be null");
        if (RESOURCE_REPOSITORY.getResourceType() != PackType.CLIENT_RESOURCES) {
            throw new IllegalArgumentException("Resource repository must have client resources");
        }

        TEXTURES = ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a resource at the root of this pack.
     * @param resourceNames      name of resources
     * @return the resource with that name or null if not found
     */
    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... resourceNames) {
        requireNonNull(resourceNames, "Resource name cannot be null");
        return null;
    }

    /**
     * Gets a resource from this pack. Only mod-controlled textures provided in
     * the constructor can be found. .mcmeta files for those textures are replaced
     * with dummy metadata.
     * @param packType      client or server resources. Only client resources are available.
     * @param location      location of the resource to retrieve
     * @return the requested resource
     */
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(location, "Location cannot be null");

        if (packType != PackType.CLIENT_RESOURCES) {
            return null;
        }

        ResourceLocation textureLocation = getTextureLocation(location);
        boolean isKnownTexture = TEXTURES.containsKey(textureLocation);
        boolean isVanillaMetadata = location.getPath().endsWith(VANILLA_METADATA_EXTENSION);

        if (isKnownTexture && isVanillaMetadata) {
            TextureData<?> textureData = TEXTURES.get(textureLocation);
            int frameWidth = textureData.getFrameWidth();
            int frameHeight = textureData.getFrameHeight();
            return () -> new ByteArrayInputStream(
                    makeEmptyAnimationJson(frameWidth, frameHeight).getBytes(StandardCharsets.UTF_8)
            );
        } else if (!isKnownTexture) {
            return null;
        }

        // Don't let a potential bug be silenced as an IOException
        if (!RESOURCE_REPOSITORY.hasResource(textureLocation)) {
            throw new IllegalStateException("A texture given to the sprite fix pack as one being controlled by " +
                    "this mod does not actually exist");
        }

        // If the texture is controlled by the mod, we already know it's in the topmost pack
        return () -> RESOURCE_REPOSITORY.getFirstCollectionWith(textureLocation).getResource(PackType.CLIENT_RESOURCES,
                textureLocation);

    }

    /**
     * Gets the locations of the available resources in this pack (the textures provided in
     * the constructor).
     *
     * @param packType          client or server resources. Only client resources are available.
     * @param namespace         namespace of the requested resources
     * @param pathStart         required start of the resources' paths (like folders, no trailing slash)
     * @param resourceOutput    output for matching resources
     */
    @Override
    public void listResources(PackType packType, String namespace, String pathStart, ResourceOutput resourceOutput) {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(resourceOutput, "Resource supplier cannot be null");

        if (packType == PackType.SERVER_DATA) {
            return;
        }

        String directoryStart = pathStart.length() > 0 ? pathStart + "/" : "";

        TEXTURES.keySet().forEach((location) -> {
            String path = location.getPath();
            boolean isRightNamespace = location.getNamespace().equals(namespace);
            boolean isRightPath = path.startsWith(directoryStart);
            if (isRightNamespace && isRightPath) {
                resourceOutput.accept(location, getResource(PackType.CLIENT_RESOURCES, location));

                ResourceLocation metadataLocation = getMetadataLocation(location);
                resourceOutput.accept(metadataLocation, getResource(PackType.CLIENT_RESOURCES, metadataLocation));
            }
        });

    }

    /**
     * Gets the unique namespaces of all the resources in this pack.
     * @param packType      client or server resources. Only client resources are available.
     * @return the unique namespaces of this pack's resources
     */
    @Override
    public Set<String> getNamespaces(PackType packType) {
        requireNonNull(packType, "Pack type cannot be null");
        return packType == PackType.CLIENT_RESOURCES ?
                TEXTURES.keySet().stream().map(ResourceLocation::getNamespace).collect(Collectors.toSet()) : Set.of();
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
    public String packId() {
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
     * Converts a texture location to the location ending with the vanilla metadata extension (.mcmeta).
     * @param location      location of texture
     * @return location of the metadata  associated with the texture
     */
    private ResourceLocation getMetadataLocation(ResourceLocation location) {
        return new ResourceLocation(
                location.getNamespace(), location.getPath() + VANILLA_METADATA_EXTENSION
        );
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