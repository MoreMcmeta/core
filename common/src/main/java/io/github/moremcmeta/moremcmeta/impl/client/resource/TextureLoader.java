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
import com.mojang.datafixers.util.Pair;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataReader;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Loads textures during resource reloading.
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
     * @param paths                 paths to search for textures in
     * @return a mapping of texture location to the texture itself
     */
    public ImmutableMap<ResourceLocation, R> load(OrderedResourceRepository resourceRepository, String... paths) {
        requireNonNull(resourceRepository, "Resource manager cannot be null");
        requireNonNull(paths, "Paths cannot be null");

        Optional<String> invalidPath = Arrays.stream(paths)
                .filter((path) -> path.isEmpty() || path.startsWith("/"))
                .findAny();
        if (invalidPath.isPresent()) {
            throw new IllegalArgumentException("Path cannot be empty or start with a slash: "+ invalidPath.get());
        }

        Set<ResourceLocation> textureCandidates = searchResources(
                resourceRepository,
                paths,
                (fileName) -> METADATA_READERS.keySet().stream().anyMatch(fileName::endsWith)
        );

        return makeTextures(textureCandidates, resourceRepository, paths);
    }

    /**
     * Searches the repository for resources at the given paths that match the given filter.
     * @param repository            repository containing resources
     * @param paths                 start of paths to search in (must not be empty)
     * @param fileNameFilter        filter that returns true when a file should be included
     * @return locations of all matching resources
     */
    private Set<ResourceLocation> searchResources(OrderedResourceRepository repository, String[] paths,
                                                  Predicate<String> fileNameFilter) {
        Set<ResourceLocation> results = new HashSet<>();

        for (String path : paths) {
            results.addAll(repository.listResources(path, fileNameFilter));
        }

        return results;
    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates           possible locations of textures
     * @param repository           resources to search through
     * @param paths                paths to search for textures in
     * @return a mapping of texture location to the texture itself
     */
    private ImmutableMap<ResourceLocation, R> makeTextures(Collection<? extends ResourceLocation> candidates,
                                                           OrderedResourceRepository repository,
                                                           String... paths) {
        Map<ResourceLocation, ReadMetadataFile> locationToMetadata = new ConcurrentHashMap<>();

        // Read metadata from unique candidates
        candidates.stream().distinct().parallel().forEach(
                (metadataLocation) -> readMetadata(repository, metadataLocation, locationToMetadata, paths)
        );

        Map<ResourceLocation, R> textures = new ConcurrentHashMap<>();
        combineByTexture(locationToMetadata).entrySet()
                .stream()
                .parallel()
                .forEach((entry) -> {
                    MetadataView metadata = entry.getValue().getFirst();
                    Map<String, Integer> smallestCollectionIndexByKey = entry.getValue().getSecond();
                    readTexture(repository, entry.getKey(), metadata, smallestCollectionIndexByKey, textures);
                });

        return ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a texture from a file and places it in the provided map.
     * @param repository           resource manager to get textures/metadata from
     * @param metadataLocation     file location of the metadata
     * @param results              filled with the result, must support concurrent modification
     * @param paths                paths to search for textures in
     */
    private void readMetadata(OrderedResourceRepository repository, ResourceLocation metadataLocation,
                              Map<ResourceLocation, ReadMetadataFile> results, String... paths) {
        PackType resourceType = repository.resourceType();

        try {
            OrderedResourceRepository.ResourceCollectionResult metadataResources = repository
                    .getFirstCollectionWith(metadataLocation);

            String metadataPath = metadataLocation.getPath();
            String extension = metadataPath.substring(metadataPath.lastIndexOf('.'));

            // There must be a reader for this extension since we only retrieved files with readers' extensions
            InputStream metadataStream = metadataResources.collection().getResource(resourceType, metadataLocation);
            Map<ResourceLocation, MetadataView> metadata = METADATA_READERS
                    .get(extension)
                    .read(metadataLocation, metadataStream, (filter) -> searchResources(repository, paths, filter));
            metadataStream.close();

            results.put(metadataLocation, new ReadMetadataFile(metadata, metadataResources.collectionIndex()));
        } catch (IOException ioException) {
            LOGGER.error("Texture associated with metadata in file {} is missing: {}",
                    metadataLocation, ioException);
        } catch (InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata in file {}: {}", metadataLocation, metadataError);
        }
    }

    /**
     * Combines metadata to produce a single {@link MetadataView} for every texture.
     * @param locationToMetadata        map of metadata location to actual metadata
     * @return combined metadata by texture
     */
    private Map<ResourceLocation, Pair<MetadataView, Map<String, Integer>>> combineByTexture(
            Map<ResourceLocation, ReadMetadataFile> locationToMetadata
    ) {

        // Organize metadata by textures; use a tree map to sort by metadata location
        Map<ResourceLocation, Map<ResourceLocation, MetadataViewAndIndex>> textureToAllMetadata = new HashMap<>();
        locationToMetadata.forEach((metadataLocation, file) ->
                file.METADATA_BY_TEXTURE.forEach((textureLocation, metadata) ->
                        textureToAllMetadata
                                .computeIfAbsent(textureLocation, (location) -> new TreeMap<>())
                                .put(metadataLocation, new MetadataViewAndIndex(metadata, file.COLLECTION_INDEX))
                )
        );

        // Combine the metadata
        Map<ResourceLocation, Pair<MetadataView, Map<String, Integer>>> textureToCombinedMetadata = new HashMap<>();
        textureToAllMetadata.forEach((textureLocation, allMetadata) -> {
            Pair<Optional<String>, Map<String, Integer>> duplicateKeyAndCollectionIndices = findDuplicateKey(
                    allMetadata.values()
            );
            Optional<String> duplicateKey = duplicateKeyAndCollectionIndices.getFirst();

            if (duplicateKey.isPresent()) {
                LOGGER.error(
                        "Two metadata files for the same texture {} in the same pack have conflicting keys: {}",
                        textureLocation,
                        duplicateKey.get()
                );
                return;
            }

            textureToCombinedMetadata.put(
                    textureLocation,
                    Pair.of(
                            new CombinedMetadataView(
                                allMetadata.values().stream()
                                        .sorted(Comparator.comparingInt((viewAndIndex) -> viewAndIndex.COLLECTION_INDEX))
                                        .map((viewAndIndex) -> viewAndIndex.VIEW)
                                        .toList()
                            ),
                            duplicateKeyAndCollectionIndices.getSecond()
                    )
            );
        });

        return textureToCombinedMetadata;
    }

    /**
     * Finds a key that is present at the top level of two {@link MetadataView}s, if any.
     * @param metadataViews     metadata views to check for duplicates
     * @return duplicate key, if any and the index of the collection containing the section name,
     *         where smaller indices indicate a higher pack
     */
    private Pair<Optional<String>, Map<String, Integer>> findDuplicateKey(
            Collection<MetadataViewAndIndex> metadataViews
    ) {
        Map<String, Integer> smallestIndex = new HashMap<>();
        Map<String, Integer> countByIndex = new HashMap<>();

        for (MetadataViewAndIndex viewAndIndex : metadataViews) {
            for (String key : viewAndIndex.VIEW.keys()) {
                int previousSmallestIndex = smallestIndex.getOrDefault(key, Integer.MAX_VALUE);
                int currentIndex = viewAndIndex.COLLECTION_INDEX;
                if (currentIndex < previousSmallestIndex) {
                    smallestIndex.put(key, currentIndex);
                    countByIndex.put(key, 1);
                } else if (currentIndex == previousSmallestIndex) {
                    countByIndex.put(key, countByIndex.get(key) + 1);
                }
            }
        }

        return Pair.of(
                countByIndex.entrySet().stream()
                    .filter((entry) -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .findAny(),
                smallestIndex
        );
    }

    /**
     * Gets a texture from a file and places it in the provided map.
     * @param resourceRepository            resource manager to get textures/metadata from
     * @param textureLocation               file location of the texture
     * @param metadata                      metadata associated with the texture
     * @param collectionIndexBySection      index of the collection containing the section name,
     *                                      where smaller indices indicate a higher pack
     * @param results              filled with the result, must support concurrent modification
     */
    private void readTexture(OrderedResourceRepository resourceRepository, ResourceLocation textureLocation,
                             MetadataView metadata, Map<String, Integer> collectionIndexBySection,
                             Map<ResourceLocation, R> results) {
        PackType resourceType = resourceRepository.resourceType();

        try {
            OrderedResourceRepository.ResourceCollectionResult collectionResult = resourceRepository
                    .getFirstCollectionWith(textureLocation);

            int textureCollectionIndex = collectionResult.collectionIndex();
            Set<String> sectionsInSameCollection = collectionIndexBySection
                    .entrySet()
                    .stream()
                    .filter((entry) -> entry.getValue() == textureCollectionIndex)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            InputStream textureStream = collectionResult
                    .collection()
                    .getResource(resourceType, textureLocation);
            R texture = TEXTURE_READER.read(textureStream, metadata, sectionsInSameCollection);
            textureStream.close();

            results.put(textureLocation, texture);
        } catch (IOException ioException) {
            LOGGER.error("Unable to read texture {}: {}",
                    textureLocation, ioException);
        } catch (InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata for texture {}: {}", textureLocation, metadataError);
        }
    }

    /**
     * Holds multiple {@link MetadataView}s and the index of the
     * collection that contains the metadata file they came from.
     * @author soir20
     */
    private static class ReadMetadataFile {
        public final Map<ResourceLocation, MetadataView> METADATA_BY_TEXTURE;
        public final int COLLECTION_INDEX;

        /**
         * Creates a new wrapper for {@link MetadataView}s and collection index.
         * @param metadataByTexture         metadata views according to the path of their associated texture
         * @param collectionIndex           index of the collection that the metadata views came from
         */
        public ReadMetadataFile(Map<ResourceLocation, MetadataView> metadataByTexture, int collectionIndex) {
            METADATA_BY_TEXTURE = metadataByTexture;
            COLLECTION_INDEX = collectionIndex;
        }

    }

    /**
     * Holds a {@link MetadataView} and the index of the
     * collection that contains the metadata file it came from.
     * @author soir20
     */
    private static class MetadataViewAndIndex {
        public final MetadataView VIEW;
        public final int COLLECTION_INDEX;

        /**
         * Creates a new wrapper for a {@link MetadataView} and collection index.
         * @param view      metadata view
         * @param index     index of the collection that the metadata view came from
         */
        public MetadataViewAndIndex(MetadataView view, int index) {
            VIEW = view;
            COLLECTION_INDEX = index;
        }

    }

}
