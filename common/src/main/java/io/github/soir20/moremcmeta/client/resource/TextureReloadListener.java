package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import io.github.soir20.moremcmeta.client.io.ITextureReader;
import io.github.soir20.moremcmeta.client.texture.IManager;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Uploads textures to the texture manager on texture reloading 
 * so that they will always be used for rendering.
 * @param <R> resource type
 * @author soir20
 */
public class TextureReloadListener<R> implements ResourceManagerReloadListener {
    private static final String METADATA_EXTENSION = ".moremcmeta";

    private final IManager<R> TEXTURE_MANAGER;
    private final ITextureReader<R> TEXTURE_READER;
    private final Logger LOGGER;
    private final Map<ResourceLocation, R> LAST_TEXTURES_ADDED;

    /**
     * Creates a TextureReloadListener.
     * @param texReader             reads textures
     * @param texManager            uploads textures to the game's texture manager
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureReloadListener(ITextureReader<R> texReader, IManager<R> texManager,
                                 Logger logger) {
        TEXTURE_READER = requireNonNull(texReader, "Texture reader cannot be null");
        TEXTURE_MANAGER = requireNonNull(texManager, "Texture manager cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
        LAST_TEXTURES_ADDED = new HashMap<>();
    }

    /**
     * Uploads textures to the texture manager when the game's textures reload. This event only
     * fires for client-side resources, not data packs.
     * @param resourceManager       the game's central resource manager
     */
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        requireNonNull(resourceManager, "Resource manager cannot be null");

        Collection<ResourceLocation> textureCandidates = new HashSet<>();

        /* We should catch ResourceLocation errors to prevent bad texture names/paths from
           removing all resource packs. We can't filter invalid folder names, so we don't filter
           invalid texture names for consistency.
           NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
        try {
             textureCandidates.addAll(resourceManager.listResources(
                    "textures",
                    fileName -> fileName.endsWith(METADATA_EXTENSION)
             ));

             // Compatibility fix since OptiFine stores its textures in a different folder
             textureCandidates.addAll(resourceManager.listResources(
                     "optifine",
                     fileName -> fileName.endsWith(METADATA_EXTENSION)
             ));

        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name; no textures will be loaded: {}",
                    error.toString());
            return;
        } finally {

            // Clean up any previously loaded textures
            LAST_TEXTURES_ADDED.keySet().forEach(TEXTURE_MANAGER::unregister);
            LAST_TEXTURES_ADDED.clear();

        }

        ImmutableMap<ResourceLocation, R> textures = getTextures(textureCandidates, resourceManager);

        // Load the textures after ticker successfully created
        LAST_TEXTURES_ADDED.putAll(textures);
        textures.forEach(TEXTURE_MANAGER::register);

    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates        possible locations of textures
     * @param resourceManager   the resource manager for the current reload
     */
    private ImmutableMap<ResourceLocation, R> getTextures(Collection<ResourceLocation> candidates,
                                                                     ResourceManager resourceManager) {
        ImmutableMap.Builder<ResourceLocation, R> textures = new ImmutableMap.Builder<>();

        // Create textures from unique candidates
        (new HashSet<>(candidates)).forEach((metadataLocation) -> {
            ResourceLocation textureLocation = new ResourceLocation(metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(METADATA_EXTENSION, ""));

            Optional<R> texture = getTexture(resourceManager, textureLocation,
                    metadataLocation);

            // Keep track of which textures are created
            texture.ifPresent(tex -> textures.put(textureLocation, tex));
        });

        return textures.build();
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
