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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import io.github.soir20.moremcmeta.client.io.ITextureReader;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Loads and queues animated textures during resource reloading.
 * @param <R> resource type
 * @author soir20
 */
public class TextureLoader<R> {
    private static final String METADATA_EXTENSION = ".moremcmeta";

    private final ITextureReader<R> TEXTURE_READER;
    private final Logger LOGGER;

    /**
     * Creates a TextureLoader.
     * @param texReader             reads textures
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureLoader(ITextureReader<R> texReader, Logger logger) {
        TEXTURE_READER = requireNonNull(texReader, "Texture reader cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Searches for and loads animated textures from a folder throughout all resource packs.
     * @param resourceManager       the game's central resource manager
     * @param path                  the path to search for textures in
     */
    public ImmutableMap<ResourceLocation, R> load(ResourceManager resourceManager, String path) {
        requireNonNull(resourceManager, "Resource manager cannot be null");

        Collection<ResourceLocation> textureCandidates;

        /* We should catch ResourceLocation errors to prevent bad texture names/paths from
           removing all resource packs. We can't filter invalid folder names, so we don't filter
           invalid texture names for consistency.
           NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
        try {
             textureCandidates = resourceManager.listResources(
                    path,
                    fileName -> fileName.endsWith(METADATA_EXTENSION)
             );

        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name; no textures will be loaded: {}",
                    error.toString());
            return ImmutableMap.of();
        }

        return getTextures(textureCandidates, resourceManager);
    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates        possible locations of textures
     * @param resourceManager   the resource manager for the current reload
     */
    private ImmutableMap<ResourceLocation, R> getTextures(Collection<ResourceLocation> candidates,
                                                                     ResourceManager resourceManager) {
        Map<ResourceLocation, R> textures = new ConcurrentHashMap<>();

        // Create textures from unique candidates
        candidates.stream().distinct().parallel().forEach((metadataLocation) -> {
            ResourceLocation textureLocation = new ResourceLocation(metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(METADATA_EXTENSION, ""));

            Optional<R> texture = getTexture(resourceManager, textureLocation, metadataLocation);

            // Keep track of which textures are created
            texture.ifPresent(tex -> textures.put(textureLocation, tex));

        });

        return ImmutableMap.copyOf(textures);
    }

    /**
     * Gets an texture from a file.
     * @param resourceManager   resource manager to get textures/metadata from
     * @param textureLocation   location of the image/.png texture
     * @param metadataLocation  file location of texture's metadata for this mod (not .mcmeta)
     * @return the texture, or empty if the file is not found
     */
    private Optional<R> getTexture(ResourceManager resourceManager,
                                   ResourceLocation textureLocation,
                                   ResourceLocation metadataLocation) {
        try (Resource originalResource = resourceManager.getResource(textureLocation);
             Resource metadataResource = resourceManager.getResource(metadataLocation)) {

            // We don't want to get metadata from a lower pack than the texture
            if (originalResource.getSourceName().equals(metadataResource.getSourceName())) {
                InputStream textureStream = originalResource.getInputStream();
                InputStream metadataStream = metadataResource.getInputStream();

                return Optional.of(TEXTURE_READER.read(textureStream, metadataStream));
            }
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {}: {}",
                    textureLocation, ioException);
        } catch (JsonParseException jsonError) {
            LOGGER.error("Unable to read texture metadata: {}", jsonError.toString());
        } catch (IllegalArgumentException metadataError) {
            LOGGER.error("Found invalid metadata parameter: {}", metadataError.toString());
        }

        return Optional.empty();
    }

}
