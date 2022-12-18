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

package io.github.moremcmeta.moremcmeta.impl.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureReader;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Loads animated textures during resource reloading.
 * @param <R> resource type
 * @author soir20
 */
public class TextureLoader<R> {
    private final TextureReader<? extends R> TEXTURE_READER;
    private final Map<String, ? extends MetadataReader> METADATA_READERS;
    private final Logger LOGGER;

    /**
     * Creates a TextureLoader.
     * @param textureReader         reads textures from a stream of file data
     * @param metadataReaders       {@link MetadataReader}s by extension. All extensions must start with a
     *                              period (.) and contain at least one other character.
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureLoader(TextureReader<? extends R> textureReader,
                         ImmutableMap<String, ? extends MetadataReader> metadataReaders, Logger logger) {

        TEXTURE_READER = requireNonNull(textureReader, "Texture reader cannot be null");
        METADATA_READERS = requireNonNull(metadataReaders, "Metadata readers cannot be null");

        if (METADATA_READERS.keySet().stream().anyMatch((ext) -> ext.lastIndexOf('.') != 0 || ext.length() < 2)) {
            throw new IllegalArgumentException("File extensions must contain only one period (.) at the start and " +
                    "contain least one other character");
        }

        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Searches for and loads textures from a folder throughout all resource packs.
     * @param resourceRepository    resources to search through
     * @param path                  the path to search for textures in
     * @param previousResults       previous results taken into account when checking for multiple metadata files for
     *                              the same texture
     * @return a mapping of texture location to the texture itself
     */
    public ImmutableMap<ResourceLocation, R> load(OrderedResourceRepository resourceRepository, String path,
                                                  ImmutableMap<ResourceLocation, R> previousResults) {
        requireNonNull(resourceRepository, "Resource manager cannot be null");
        requireNonNull(path, "Path cannot be null");
        requireNonNull(previousResults, "Previous results cannot be null");

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
                    fileName ->  METADATA_READERS.keySet().stream().anyMatch(fileName::endsWith)
             );

        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name; no textures will be loaded: {}",
                    error.toString());
            return ImmutableMap.of();
        }

        return getTextures(textureCandidates, resourceRepository, previousResults);
    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates           possible locations of textures
     * @param resourceRepository   resources to search through
     * @param previousResults      previous results taken into account when checking for multiple metadata files for
     *                             the same texture
     * @return a mapping of texture location to the texture itself
     */
    private ImmutableMap<ResourceLocation, R> getTextures(Collection<? extends ResourceLocation> candidates,
                                                          OrderedResourceRepository resourceRepository,
                                                          Map<ResourceLocation, R> previousResults) {
        Map<ResourceLocation, R> textures = new ConcurrentHashMap<>(previousResults);

        // Create textures from unique candidates
        candidates.stream().distinct().parallel().forEach(
                (metadataLocation) -> texture(resourceRepository, metadataLocation, textures)
        );

        return ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a texture from a file and places it in the provided map.
     * @param resourceRepository   resource manager to get textures/metadata from
     * @param metadataLocation     file location of texture's metadata for this mod (not .mcmeta)
     * @param results              filled with the result
     */
    private void texture(OrderedResourceRepository resourceRepository, ResourceLocation metadataLocation,
                         Map<ResourceLocation, R> results) {
        PackType resourceType = resourceRepository.resourceType();

        try {
            ResourceCollection metadataResources = resourceRepository.getFirstCollectionWith(metadataLocation);

            InputStream metadataStream = metadataResources.getResource(resourceType, metadataLocation);
            String metadataPath = metadataLocation.getPath();
            String extension = metadataPath.substring(metadataPath.lastIndexOf('.'));

            MetadataReader.ReadMetadata metadata = METADATA_READERS
                    .get(extension)
                    .read(metadataLocation, metadataStream);

            ResourceLocation textureLocation = metadata.textureLocation();

            if (resourceRepository.getFirstCollectionWith(textureLocation).equals(metadataResources)) {
                InputStream textureStream = metadataResources.getResource(resourceType, textureLocation);

                // There must be a reader for this extension since we only retrieved files with readers' extensions
                R texture = TEXTURE_READER.read(textureStream, metadata.metadata());

                textureStream.close();
                metadataStream.close();

                if (results.containsKey(textureLocation)) {
                    results.remove(textureLocation);

                    throw new InvalidMetadataException(
                            String.format(
                                    "Two metadata files found for same texture (%s). Neither will be applied",
                                    textureLocation
                            )
                    );
                }

                results.put(textureLocation, texture);
            }
        } catch (IOException ioException) {
            LOGGER.error("Texture associated with metadata in file {} is missing: {}",
                    metadataLocation, ioException);
        } catch (InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata in file {}: {}", metadataLocation, metadataError);
        }
    }

}