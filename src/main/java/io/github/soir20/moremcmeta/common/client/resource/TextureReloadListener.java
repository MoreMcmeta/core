package io.github.soir20.moremcmeta.common.client.resource;

import com.google.common.collect.ImmutableMap;
import io.github.soir20.moremcmeta.common.client.renderer.texture.ITextureManager;
import io.github.soir20.moremcmeta.common.client.io.ITextureReader;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Uploads textures to the texture manager on texture reloading 
 * so that they will always be used for rendering.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TextureReloadListener implements ISelectiveResourceReloadListener {
    private static final String METADATA_EXTENSION = ".moremcmeta";

    private final ITextureManager TEXTURE_MANAGER;
    private final ITextureReader<Texture> TEXTURE_READER;
    private final Logger LOGGER;
    private final Map<ResourceLocation, Texture> LAST_TEXTURES_ADDED;

    /**
     * Creates a TextureReloadListener.
     * @param texReader             reads textures
     * @param texManager            uploads textures to the game's texture manager
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureReloadListener(ITextureReader<Texture> texReader, ITextureManager texManager,
                                 Logger logger) {
        TEXTURE_READER = requireNonNull(texReader, "Texture reader cannot be null");
        TEXTURE_MANAGER = requireNonNull(texManager, "Texture manager cannot be null");
        LOGGER = requireNonNull(logger, "Logger cannot be null");
        LAST_TEXTURES_ADDED = new HashMap<>();
    }

    /**
     * Gets the resource type whose reloading will trigger this listener.
     * @return the resource type for this listener
     */
    @Override
    public IResourceType getResourceType()
    {
        return VanillaResourceType.TEXTURES;
    }

    /**
     * Uploads textures to the texture manager when the game's textures reload. This event only
     * fires for client-side resources, not data packs.
     * @param resourceManager       the game's central resource manager
     * @param resourcePredicate     tests whether the listener should reload for this resource type
     */
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager,
                                        Predicate<IResourceType> resourcePredicate) {
        requireNonNull(resourceManager, "Resource manager cannot be null");
        requireNonNull(resourcePredicate, "Resource predicate cannot be null");

        if (!resourcePredicate.test(getResourceType())) {
            return;
        }

        Collection<ResourceLocation> textureCandidates;

        /* We should catch ResourceLocation errors to prevent bad texture names/paths from
           removing all resource packs. We can't filter invalid folder names, so we don't filter
           invalid texture names for consistency.
           NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
        try {
             textureCandidates = resourceManager.getAllResourceLocations(
                    "textures",
                    fileName -> fileName.endsWith(METADATA_EXTENSION)
             );
        } catch (ResourceLocationException error) {
            LOGGER.error("Found texture with invalid name; no textures will be loaded: {}",
                    error.toString());
            return;
        } finally {

            // Clean up any previously loaded textures
            LAST_TEXTURES_ADDED.keySet().forEach(TEXTURE_MANAGER::deleteTexture);
            LAST_TEXTURES_ADDED.clear();

        }

        ImmutableMap<ResourceLocation, Texture> textures = getTextures(textureCandidates, resourceManager);

        // Load the textures after ticker successfully created
        LAST_TEXTURES_ADDED.putAll(textures);
        textures.forEach(TEXTURE_MANAGER::loadTexture);

    }

    /**
     * Creates all valid textures from candidates.
     * @param candidates        possible locations of textures
     * @param resourceManager   the resource manager for the current reload
     */
    private ImmutableMap<ResourceLocation, Texture> getTextures(Collection<ResourceLocation> candidates,
                                                                IResourceManager resourceManager) {
        ImmutableMap.Builder<ResourceLocation, Texture> textures = new ImmutableMap.Builder<>();

        // Create textures from unique candidates
        (new HashSet<>(candidates)).forEach((metadataLocation) -> {
            ResourceLocation textureLocation = new ResourceLocation(metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(METADATA_EXTENSION, ""));

            Optional<Texture> texture = getTexture(resourceManager, textureLocation, metadataLocation);

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
    private Optional<Texture> getTexture(IResourceManager resourceManager,
                                                 ResourceLocation textureLocation, ResourceLocation metadataLocation) {
        try (IResource originalResource = resourceManager.getResource(textureLocation);
             IResource metadataResource = resourceManager.getResource(metadataLocation)) {

            // We don't want to get metadata from a lower pack than the texture
            if (originalResource.getPackName().equals(metadataResource.getPackName())) {
                InputStream textureStream = originalResource.getInputStream();
                InputStream metadataStream = metadataResource.getInputStream();

                return Optional.of(TEXTURE_READER.read(textureStream, metadataStream));
            }
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {}: {}",
                    textureLocation, ioException);
        }

        return Optional.empty();
    }

}
