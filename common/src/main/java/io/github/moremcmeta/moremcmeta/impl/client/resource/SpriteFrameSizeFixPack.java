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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Provides dummy metadata for MoreMcmeta-controlled atlas sprites when vanilla metadata is requested.
 * This prevents the atlas sprites from being "squished" because only the first frame is stitched
 * onto the atlas, not all of them.
 * @author soir20
 */
@MethodsReturnNonnullByDefault
public final class SpriteFrameSizeFixPack implements PackResources {
    private static final String VANILLA_METADATA_EXTENSION = ".mcmeta";
    private final ImmutableMap<? extends ResourceLocation, ? extends TextureData<?>> TEXTURES;
    private final ImmutableMap<? extends String, ? extends StreamSource> ROOT_RESOURCES;

    /**
     * Creates a new sprite fix pack.
     * @param textures              textures controlled by the mod. Every texture must have an image set.
     * @param rootResources         root resources for this pack
     */
    public SpriteFrameSizeFixPack(Map<? extends ResourceLocation, ? extends TextureData<?>> textures,
                                  Map<? extends String, ? extends StreamSource> rootResources) {
        requireNonNull(textures, "Textures cannot be null");
        requireNonNull(rootResources, "Root resources cannot be null");
        TEXTURES = ImmutableMap.copyOf(textures);
        ROOT_RESOURCES = ImmutableMap.copyOf(rootResources);
    }

    /**
     * Gets a resource at the root of this pack.
     * @param resourceName      name of resource
     * @return the resource with that name or null if not found
     */
    @Nullable
    @Override
    public InputStream getRootResource(String resourceName) throws IOException {
        requireNonNull(resourceName, "Resource name cannot be null");

        if (ROOT_RESOURCES.containsKey(resourceName)) {
            return ROOT_RESOURCES.get(resourceName).get();
        }

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

        Optional<ResourceLocation> textureLocationOptional = textureLocation(location);
        if (textureLocationOptional.isEmpty()) {
            throw new IOException("Requested invalid resource location: " + location);
        }

        ResourceLocation textureLocation = textureLocationOptional.get();
        TextureData<?> textureData = TEXTURES.get(textureLocation);

        boolean isKnownTexture = textureData != null;
        boolean isVanillaMetadata = location.getPath().endsWith(VANILLA_METADATA_EXTENSION);

        if (isKnownTexture && isVanillaMetadata) {
            int frameWidth = textureData.frameSize().width();
            int frameHeight = textureData.frameSize().height();
            return new ByteArrayInputStream(
                    makeEmptyAnimationJson(frameWidth, frameHeight).getBytes(StandardCharsets.UTF_8)
            );
        }

        throw new IOException("Resource not found in sprite fix pack: " + location);
    }

    /**
     * Gets the locations of the available resources in this pack (the textures provided in
     * the constructor).
     * @param packType          client or server resources. Only client resources are available.
     * @param namespace         namespace of the requested resources
     * @param pathStart         required start of the resources' paths (like folders, no trailing slash)
     * @param pathFilter        filter for entire paths, including the file name and extension
     * @return the matching resources in this pack
     */
    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String pathStart,
                                                     Predicate<ResourceLocation> pathFilter) {
        requireNonNull(packType, "Pack type cannot be null");
        requireNonNull(namespace, "Namespace cannot be null");
        requireNonNull(pathStart, "Path start cannot be null");
        requireNonNull(pathFilter, "Path filter cannot be null");
        return ImmutableList.of();
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

        Optional<ResourceLocation> textureLocationOptional = textureLocation(location);
        return textureLocationOptional.isPresent() && TEXTURES.containsKey(textureLocationOptional.get())
                && location.getPath().endsWith(VANILLA_METADATA_EXTENSION);
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
    private Optional<ResourceLocation> textureLocation(ResourceLocation location) {

        // Some mods allow invalid resource locations to be created, so we ignore them like vanilla packs do
        try {
            return Optional.of(new ResourceLocation(
                    location.getNamespace(), location.getPath().replace(VANILLA_METADATA_EXTENSION, "")
            ));
        } catch (ResourceLocationException err) {
            return Optional.empty();
        }

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