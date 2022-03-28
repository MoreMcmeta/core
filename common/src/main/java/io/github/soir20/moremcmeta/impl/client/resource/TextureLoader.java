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

package io.github.soir20.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.impl.client.io.TextureReader;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Loads animated textures during resource reloading.
 * @param <R> resource type
 * @author soir20
 */
public class TextureLoader<R> {
    private static final String IMAGE_EXTENSION = ".png";
    private static final String METADATA_EXTENSION = IMAGE_EXTENSION +  ".moremcmeta";

    private final TextureReader<? extends R> TEXTURE_READER;
    private final Logger LOGGER;

    /**
     * Creates a TextureLoader.
     * @param texReader             reads textures
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureLoader(TextureReader<? extends R> texReader, Logger logger) {
        TEXTURE_READER = requireNonNull(texReader, "Texture reader cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Searches for and loads animated textures from a folder throughout all resource packs.
     * @param resourceRepository    resources to search through
     * @param path                  the path to search for textures in
     */
    public ImmutableMap<ResourceLocation, R> load(OrderedResourceRepository resourceRepository, String path) {
        requireNonNull(resourceRepository, "Resource manager cannot be null");
        requireNonNull(path, "Path cannot be null");
        if (path.isEmpty() || path.startsWith("/")) {
            throw new IllegalArgumentException("Path cannot be empty or start with a slash");
        }

        Collection<ResourceLocation> textureCandidates;

        /* We should catch ResourceLocation errors to prevent bad texture names/paths from
           removing all resource packs. We can't filter invalid folder names, so we don't filter
           invalid texture names for consistency.
           NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
        try {
             textureCandidates = resourceRepository.listResources(
                    path,
                    fileName -> fileName.endsWith(METADATA_EXTENSION)
             );

        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name; no textures will be loaded: {}",
                    error.toString());
            return ImmutableMap.of();
        }

        return getTextures(textureCandidates, resourceRepository);
    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates           possible locations of textures
     * @param resourceRepository   resources to search through
     */
    private ImmutableMap<ResourceLocation, R> getTextures(Collection<? extends ResourceLocation> candidates,
                                                          OrderedResourceRepository resourceRepository) {
        Map<ResourceLocation, R> textures = new ConcurrentHashMap<>();

        // Create textures from unique candidates
        candidates.stream().distinct().parallel().forEach((metadataLocation) -> {
            ResourceLocation textureLocation = new ResourceLocation(metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(METADATA_EXTENSION, IMAGE_EXTENSION));

            Optional<R> texture = getTexture(resourceRepository, textureLocation, metadataLocation);

            // Keep track of which textures are created
            texture.ifPresent(tex -> textures.put(textureLocation, tex));

        });

        return ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a texture from a file.
     * @param resourceRepository   resource manager to get textures/metadata from
     * @param textureLocation      location of the image/.png texture
     * @param metadataLocation     file location of texture's metadata for this mod (not .mcmeta)
     * @return the texture, or empty if the file is not found
     */
    private Optional<R> getTexture(OrderedResourceRepository resourceRepository,
                                   ResourceLocation textureLocation,
                                   ResourceLocation metadataLocation) {
        PackType resourceType = resourceRepository.getResourceType();

        try {
            ResourceCollection resources = resourceRepository.getFirstCollectionWith(textureLocation);

            if (resources.hasResource(resourceType, metadataLocation)) {
                InputStream textureStream = resources.getResource(resourceType, textureLocation);
                InputStream metadataStream = resources.getResource(resourceType, metadataLocation);

                Optional<R> texture = Optional.of(TEXTURE_READER.read(textureStream, metadataStream));

                textureStream.close();
                metadataStream.close();

                return texture;
            }
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {}: {}",
                    textureLocation, ioException);
        } catch (TextureReader.InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata for texture {}: {}", textureLocation, metadataError);
        }

        return Optional.empty();
    }

}
