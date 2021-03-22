package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.client.ClientTicker;
import io.github.soir20.moremcmeta.client.renderer.texture.ITextureManager;
import io.github.soir20.moremcmeta.client.renderer.texture.ITextureReader;
import net.minecraft.client.renderer.texture.ITickable;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Uploads animated textures to the texture manager on texture reloading so they will always be used for
 * rendering.
 * @param <T>   tickable texture type
 * @author soir20
 */
public class TextureReloadListener<T extends Texture & ITickable> implements ISelectiveResourceReloadListener {
    private static final String METADATA_EXTENSION = ".moremcmeta";

    private final ITextureManager TEXTURE_MANAGER;
    private final ITextureReader<T> TEXTURE_READER;
    private final Function<ImmutableCollection<T>, ClientTicker> TICKER_FACTORY;
    private final Logger LOGGER;
    private final Set<ResourceLocation> LAST_TEXTURES_ADDED;

    private ClientTicker lastTicker;

    /**
     * Creates a TextureReloadListener.
     * @param texReader             reads animated textures
     * @param texManager            uploads animated textures to the game's texture manager
     * @param tickerFactory         creates tickers to update the animated textures
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureReloadListener(ITextureReader<T> texReader, ITextureManager texManager,
                                 Function<ImmutableCollection<T>, ClientTicker> tickerFactory,
                                 Logger logger) {
        TEXTURE_READER = texReader;
        TEXTURE_MANAGER = texManager;
        TICKER_FACTORY = tickerFactory;
        LOGGER = logger;
        LAST_TEXTURES_ADDED = new HashSet<>();
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
     * Uploads animated textures to the texture manager when the game's textures reload. This event only
     * fires for client-side resources, not data packs.
     * @param resourceManager       the game's central resource manager
     * @param resourcePredicate     tests whether the listener should reload for this resource type
     */
    @Override
    @ParametersAreNonnullByDefault
    public void onResourceManagerReload(IResourceManager resourceManager,
                                        Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(getResourceType())) {
            Collection<ResourceLocation> animationCandidates;

            /* We should catch ResourceLocation errors to prevent bad texture names/paths from
               removing all resource packs. We can't filter invalid folder names, so we don't filter
               invalid texture names for consistency.
               NOTE: Some pack types (like FolderPack) handle bad locations before we see them. */
            try {
                 animationCandidates = resourceManager.getAllResourceLocations(
                        "textures",
                        fileName -> fileName.endsWith(METADATA_EXTENSION)
                 );
            } catch (ResourceLocationException error) {
                LOGGER.error("Found texture with invalid name; no textures will be animated: {}",
                        error.toString());
                return;
            } finally {

                // Clean up any previously loaded textures because they may no longer be animated
                LAST_TEXTURES_ADDED.forEach(TEXTURE_MANAGER::deleteTexture);
                LAST_TEXTURES_ADDED.clear();

                // Always stop updating old textures
                if (lastTicker != null) {
                    lastTicker.stopTicking();
                }

            }

            ImmutableSet<T> uploadedTextures = uploadTextures(animationCandidates, resourceManager);

            // Only start ticking new textures if there are any
            if (LAST_TEXTURES_ADDED.size() > 0) {
                lastTicker = TICKER_FACTORY.apply(uploadedTextures);
            }

        }
    }

    /**
     * Creates and uploads textures to the texture manager based on their locations.
     * @param candidates        possible locations of animated textures
     * @param resourceManager   the resource manager for the current reload
     * @return  all the animated textures that were uploaded
     */
    private ImmutableSet<T> uploadTextures(Collection<ResourceLocation> candidates,
                                           IResourceManager resourceManager) {
        ImmutableSet.Builder<T> uploadedTextures = ImmutableSet.builder();

        // Load all animated textures into the tex manager so they are used for rendering
        (new HashSet<>(candidates)).forEach((metadataLocation) -> {
            ResourceLocation textureLocation = new ResourceLocation(metadataLocation.getNamespace(),
                    metadataLocation.getPath().replace(METADATA_EXTENSION, ""));

            Optional<T> animatedTexture = getAnimatedTexture(resourceManager, textureLocation, metadataLocation);

            if (animatedTexture.isPresent()) {

                // Keep track of which textures are added
                LAST_TEXTURES_ADDED.add(textureLocation);
                uploadedTextures.add(animatedTexture.get());

                // Actually upload the texture
                TEXTURE_MANAGER.loadTexture(textureLocation, animatedTexture.get());

            }
        });

        return uploadedTextures.build();
    }

    /**
     * Gets an animated texture from a file if the texture is animated.
     * @param resourceManager   resource manager to get textures/metadata from
     * @param textureLocation   location of the image/.png texture
     * @param metadataLocation  file location of texture's metadata for this mod (not .mcmeta)
     * @return the animated texture, or empty if the file is not animated/not found
     */
    private Optional<T> getAnimatedTexture(IResourceManager resourceManager,
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
