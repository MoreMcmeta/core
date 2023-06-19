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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import io.github.moremcmeta.moremcmeta.api.client.metadata.InvalidMetadataException;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataParser;
import io.github.moremcmeta.moremcmeta.api.client.metadata.MetadataView;
import io.github.moremcmeta.moremcmeta.api.client.metadata.ResourceRepository;
import io.github.moremcmeta.moremcmeta.impl.client.io.TextureReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Loads textures during resource reloading.
 * @param <R> resource type
 * @author soir20
 */
public final class TextureLoader<R> {
    private final TextureReader<? extends R> TEXTURE_READER;
    private final Map<String, ? extends MetadataParser> PARSERS;
    private final Logger LOGGER;

    /**
     * Creates a TextureLoader.
     * @param textureReader         reads textures from a stream of file data
     * @param metadataParsers       {@link MetadataParser}s by extension. All extensions must start with a
     *                              period (.) and contain at least one other character.
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureLoader(TextureReader<? extends R> textureReader,
                         ImmutableMap<String, ? extends MetadataParser> metadataParsers, Logger logger) {
        TEXTURE_READER = requireNonNull(textureReader, "Texture reader cannot be null");
        PARSERS = requireNonNull(metadataParsers, "Metadata parsers cannot be null");

        if (PARSERS.keySet().stream().anyMatch((ext) -> ext.lastIndexOf('.') != 0 || ext.length() < 2)) {
            throw new IllegalArgumentException("File extensions must contain only one period (.) at the start and " +
                    "contain least one other character");
        }

        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Searches for and loads textures from a folder throughout all resource packs.
     * @param repository            resources to search through
     * @param paths                 paths to search for textures in
     * @return a mapping of texture location to the texture itself
     */
    public ImmutableMap<ResourceLocation, R> load(OrderedResourceRepository repository, String... paths) {
        requireNonNull(repository, "Resource manager cannot be null");
        requireNonNull(paths, "Paths cannot be null");

        Optional<String> invalidPath = Arrays.stream(paths)
                .filter((path) -> path.isEmpty() || path.startsWith("/"))
                .findAny();
        if (invalidPath.isPresent()) {
            throw new IllegalArgumentException("Path cannot be empty or start with a slash: " + invalidPath.get());
        }

        Set<ResourceLocation> textureCandidates = searchResources(
                repository,
                paths,
                (fileName) -> PARSERS.keySet().stream().anyMatch(fileName::endsWith)
        );

        return makeTextures(textureCandidates, repository, paths);
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
            results.addAll(repository.list(path, fileNameFilter));
        }

        return results;
    }

    /**
     * Wraps the {@link OrderedResourceRepository} in a more limited interface to pass on to external plugins.
     * @param original      original repository to wrap
     * @param paths         start of paths to search in (must not be empty)
     * @return wrapped resource repository
     */
    private ResourceRepository wrapRepository(OrderedResourceRepository original, String[] paths) {
        return new ResourceRepository() {
            @Override
            public Optional<Pack> highestPackWith(ResourceLocation location) {
                try {
                    ResourceCollection pack = original.firstCollectionWith(location).collection();
                    return Optional.of(
                            (locationInPack) -> {
                                try {
                                    return Optional.of(pack.find(original.resourceType(), locationInPack));
                                } catch (IOException err) {
                                    return Optional.empty();
                                }
                            }
                    );
                } catch (IOException err) {
                    return Optional.empty();
                }
            }

            @Override
            public Set<ResourceLocation> list(Predicate<String> fileFilter) {
                return searchResources(original, paths, fileFilter);
            }
        };
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
        combineByTexture(repository, locationToMetadata).entrySet()
                .stream()
                .parallel()
                .forEach((entry) -> readTexture(repository, entry.getKey(), entry.getValue(), textures));

        return ImmutableMap.copyOf(textures);
    }

    /**
     * Gets a texture from a file and places it in the provided map.
     * @param repository           resource repository to get textures/metadata from
     * @param metadataLocation     file location of the metadata
     * @param results              filled with the result, must support concurrent modification
     * @param paths                paths to search for textures in
     */
    private void readMetadata(OrderedResourceRepository repository, ResourceLocation metadataLocation,
                              Map<ResourceLocation, ReadMetadataFile> results, String... paths) {
        PackType resourceType = repository.resourceType();

        try {
            OrderedResourceRepository.ResourceCollectionResult metadataResources = repository
                    .firstCollectionWith(metadataLocation);

            String metadataPath = metadataLocation.getPath();
            String extension = metadataPath.substring(metadataPath.lastIndexOf('.'));

            // There must be a parser for this extension since we only retrieved files with parsers' extensions
            InputStream metadataStream = metadataResources.collection().find(resourceType, metadataLocation);
            Map<? extends ResourceLocation, ? extends MetadataView> metadata = PARSERS
                    .get(extension)
                    .parse(metadataLocation, metadataStream, wrapRepository(repository, paths));
            metadataStream.close();

            results.put(metadataLocation, new ReadMetadataFile(metadata, metadataResources.collectionIndex(), extension));
        } catch (IOException ioException) {
            LOGGER.error("Texture associated with metadata in file {} is missing: {}",
                    metadataLocation, ioException);
        } catch (InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata in file {}: {}", metadataLocation, metadataError);
        }
    }

    /**
     * Combines metadata to produce a single {@link MetadataView} for every texture.
     * @param repository                        resource repository to get textures/metadata from
     * @param metadataLocationToMetadata        map of metadata location to actual metadata
     * @return combined metadata by texture
     */
    private Map<ResourceLocation, MetadataView> combineByTexture(
            OrderedResourceRepository repository,
            Map<ResourceLocation, ReadMetadataFile> metadataLocationToMetadata
    ) {

        // Organize metadata by textures
        Map<ResourceLocation, TextureMetadata> textureToAllMetadata = new HashMap<>();
        metadataLocationToMetadata.forEach((metadataLocation, file) ->
                file.METADATA_BY_TEXTURE.forEach((textureLocation, metadata) ->
                        textureToAllMetadata
                                .computeIfAbsent(textureLocation, (location) -> new TextureMetadata())
                                .put(
                                        metadataLocation,
                                        file.METADATA_BY_TEXTURE.get(textureLocation),
                                        file.COLLECTION_INDEX,
                                        file.EXTENSION
                                )
                )
        );

        // Combine the metadata
        Map<ResourceLocation, MetadataView> textureToCombinedMetadata = new HashMap<>();
        textureToAllMetadata.forEach((textureLocation, allMetadata) -> {
            Optional<Integer> textureIndexOptional = findCollectionIndex(repository, textureLocation);
            if (textureIndexOptional.isEmpty()) {
                LOGGER.error(
                        "Unable to find texture {} (referenced by {})",
                        textureLocation,
                        join(allMetadata.metadataLocations())
                );
                return;
            }

            int textureCollectionIndex = textureIndexOptional.get();
            allMetadata = allMetadata.metadataApplicableToTextureIn(textureCollectionIndex);

            Set<String> extensions = allMetadata.extensions();
            if (extensions.size() == 0) {

                // All metadata is in lower packs
                return;

            }

            if (extensions.size() != 1) {
                LOGGER.error(
                        "Cannot apply metadata in multiple formats to texture {} (applied {})",
                        textureLocation,
                        join(allMetadata.metadataLocations())
                );
                return;
            }

            String extension = Iterables.getOnlyElement(extensions);

            MetadataView combinedMetadata;
            if (allMetadata.size() > 1) {
                try {
                    combinedMetadata = PARSERS.get(extension)
                            .combine(textureLocation, allMetadata.metadataByLocation());
                } catch (InvalidMetadataException err) {
                    LOGGER.error(
                            "Unable to combine metadata for texture {} (applied {}): {}",
                            textureLocation,
                            join(allMetadata.metadataLocations()),
                            err
                    );
                    return;
                }
            } else {
                combinedMetadata = Iterables.getOnlyElement(allMetadata.metadataByLocation().values());
            }

            textureToCombinedMetadata.put(textureLocation, combinedMetadata);
        });

        return textureToCombinedMetadata;
    }

    /**
     * Finds the index of the first collection that contains the given resource, if any.
     * @param repository        repository to search
     * @param location          location of the resource
     * @return index of the first collection containing the resource, if any
     */
    private Optional<Integer> findCollectionIndex(OrderedResourceRepository repository,
                                                  ResourceLocation location) {
        try {
            return Optional.of(repository.firstCollectionWith(location).collectionIndex());
        } catch (IOException err) {
            return Optional.empty();
        }
    }

    /**
     * Gets a texture from a file and places it in the provided map.
     * @param repository            resource repository to get textures/metadata from
     * @param textureLocation       file location of the texture
     * @param metadata              metadata associated with the texture
     * @param results               filled with the result, must support concurrent modification
     */
    private void readTexture(OrderedResourceRepository repository, ResourceLocation textureLocation,
                             MetadataView metadata, Map<ResourceLocation, R> results) {
        PackType resourceType = repository.resourceType();

        try {
            OrderedResourceRepository.ResourceCollectionResult collectionResult = repository
                    .firstCollectionWith(textureLocation);

            InputStream textureStream = collectionResult
                    .collection()
                    .find(resourceType, textureLocation);
            R texture = TEXTURE_READER.read(textureStream, metadata);
            textureStream.close();

            results.put(textureLocation, texture);
        } catch (IOException err) {
            LOGGER.error("Unable to read texture {}: {}", textureLocation, err);
        } catch (InvalidMetadataException metadataError) {
            LOGGER.error("Invalid metadata for texture {}: {}", textureLocation, metadataError);
        }
    }

    /**
     * Joins a set of {@link ResourceLocation}s into a comma-delimited string.
     * @param locations     locations to join
     * @return all locations joined as a comma-delimited string
     */
    private static String join(Set<ResourceLocation> locations) {
        return locations.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    /**
     * Holds multiple {@link MetadataView}s and the index of the
     * collection that contains the metadata file they came from.
     * @author soir20
     */
    private static class ReadMetadataFile {
        public final Map<? extends ResourceLocation, ? extends MetadataView> METADATA_BY_TEXTURE;
        public final int COLLECTION_INDEX;
        public final String EXTENSION;

        /**
         * Creates a new wrapper for {@link MetadataView}s and collection index.
         * @param metadataByTexture         metadata views according to the path of their associated texture
         * @param collection                index of the collection that the metadata views came from
         * @param extension                 extension of the metadata files
         */
        public ReadMetadataFile(Map<? extends ResourceLocation, ? extends MetadataView> metadataByTexture,
                                int collection, String extension) {
            METADATA_BY_TEXTURE = metadataByTexture;
            COLLECTION_INDEX = collection;
            EXTENSION = extension;
        }

    }

    /**
     * Wraps per-texture metadata with utility methods.
     * @author soir20
     */
    private static class TextureMetadata {
        private final Map<ResourceLocation, Triple<MetadataView, Integer, String>> METADATA;

        /**
         * Creates a new wrapper for per-texture metadata.
         */
        public TextureMetadata() {
            METADATA = new HashMap<>();
        }

        /**
         * Adds metadata to this wrapper.
         * @param metadataLocation      location of the metadata
         * @param metadata              metadata at the given location
         * @param collection            index of collection containing the metadata
         * @param extension             extension of the file containing the metadata
         */
        public void put(ResourceLocation metadataLocation, MetadataView metadata, int collection, String extension) {
            METADATA.put(metadataLocation, Triple.of(metadata, collection, extension));
        }

        /**
         * Gets all metadata locations contained in this wrapper.
         * @return all metadata locations
         */
        public Set<ResourceLocation> metadataLocations() {
            return ImmutableSet.copyOf(METADATA.keySet());
        }

        /**
         * Gets all unique extensions among all metadata in this wrapper.
         * @return all unique extensions
         */
        public Set<String> extensions() {
            return ImmutableSet.copyOf(METADATA.values().stream().map(Triple::getRight).collect(Collectors.toSet()));
        }

        /**
         * Gets all metadata by metadata location (immutable).
         * @return all metadata by metadata location
         */
        public Map<ResourceLocation, MetadataView> metadataByLocation() {
            ImmutableMap.Builder<ResourceLocation, MetadataView> builder = new ImmutableMap.Builder<>();
            METADATA.forEach((location, triple) -> builder.put(location, triple.getLeft()));
            return builder.build();
        }

        /**
         * Gets the number of metadata files in this wrapper.
         * @return number of metadata files
         */
        public int size() {
            return METADATA.size();
        }

        /**
         * Gets a new metadata wrapper with only metadata applicable to a texture in the given collection
         * (any collection before or including the first one that contains the texture).
         * @param collection        collection index to use to filter
         * @return metadata wrapper with only applicable metadata
         */
        public TextureMetadata metadataApplicableToTextureIn(int collection) {
            return new TextureMetadata(
                    METADATA.entrySet().stream()
                            .filter((entry) -> entry.getValue().getMiddle() <= collection)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
        }

        /**
         * Creates a new wrapper for per-texture metadata.
         * @param metadata      metadata to initialize the wrapper with
         */
        private TextureMetadata(Map<ResourceLocation, Triple<MetadataView, Integer, String>> metadata) {
            METADATA = metadata;
        }

    }

}
