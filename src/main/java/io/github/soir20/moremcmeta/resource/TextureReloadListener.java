package io.github.soir20.moremcmeta.resource;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.client.renderer.texture.IAnimatedTextureFactory;
import io.github.soir20.moremcmeta.client.renderer.texture.INativeImageFactory;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Uploads animated textures to the texture manager on texture reloading so they will always be used for
 * rendering.
 * @param <T>   tickable texture type
 * @author soir20
 */
public class TextureReloadListener<T extends Texture & ITickable> implements ISelectiveResourceReloadListener {
    private final String[] FOLDERS;
    private final BiConsumer<ResourceLocation, Texture> TEXTURE_LOADER;
    private final IAnimatedTextureFactory<T> TEXTURE_FACTORY;
    private final INativeImageFactory IMAGE_FACTORY;
    private final IMetadataSectionSerializer<AnimationMetadataSection> SERIALIZER;
    private final Logger LOGGER;

    private IResourceManager resourceManager;

    /**
     * Creates a TextureReloadListener.
     * @param folders       folders to search for animated textures (exclude folders animated by default)
     * @param texLoader     uploads animated textures to the game's texture manager
     * @param texFactory    creates animated textures
     * @param serializer    serializer for .mcmeta files
     * @param logger        logs listener-related messages to the game's output
     */
    public TextureReloadListener(String[] folders, BiConsumer<ResourceLocation, Texture> texLoader,
                                 IAnimatedTextureFactory<T> texFactory, INativeImageFactory imageFactory,
                                 IMetadataSectionSerializer<AnimationMetadataSection> serializer,
                                 Logger logger) {
        FOLDERS = folders;
        TEXTURE_LOADER = texLoader;
        TEXTURE_FACTORY = texFactory;
        IMAGE_FACTORY = imageFactory;
        SERIALIZER = serializer;
        LOGGER = logger;
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
     * Uploads animated textures to the texture manager when the game's textures reload.
     * @param resManager            the game's central resource manager
     * @param resourcePredicate     tests whether the listener should reload for this resource type
     */
    @Override
    @ParametersAreNonnullByDefault
    public void onResourceManagerReload(IResourceManager resManager,
                                        Predicate<IResourceType> resourcePredicate) {
        if (resourcePredicate.test(getResourceType())) {
            resourceManager = resManager;

            for (String folder : FOLDERS) {
                Collection<ResourceLocation> animatedFiles = resourceManager.getAllResourceLocations(
                        "textures/" + folder,
                        fileName -> fileName.endsWith(".png")
                );

                animatedFiles.forEach(this::uploadToTexManager);
            }
        }
    }

    /**
     * Uploads an individual texture to the texture manager.
     * @param resourceLocation  file location of texture identical to how it is used in a entity/gui/map
     */
    private void uploadToTexManager(ResourceLocation resourceLocation) {
        try (IResource iresource = resourceManager.getResource(resourceLocation)) {
            NativeImage nativeImage = IMAGE_FACTORY.createNativeImage(iresource.getInputStream());
            AnimationMetadataSection metadata = iresource.getMetadata(SERIALIZER);

            if (metadata != null) {
                Pair<Integer, Integer> pair = metadata.getSpriteSize(nativeImage.getWidth(),
                        nativeImage.getHeight());

                T texture = TEXTURE_FACTORY.createAnimatedTexture(resourceLocation, pair.getFirst(),
                        pair.getSecond(), metadata, 0, nativeImage);

                TEXTURE_LOADER.accept(resourceLocation, texture);
            }
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to parse animation metadata from {} : {}",
                    resourceLocation, runtimeException);
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {} : {}",
                    resourceLocation, ioException);
        }
    }

}
