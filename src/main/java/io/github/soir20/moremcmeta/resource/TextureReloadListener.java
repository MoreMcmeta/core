package io.github.soir20.moremcmeta.resource;

import io.github.soir20.moremcmeta.client.renderer.texture.ITextureReader;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
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
import java.io.InputStream;
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
    private final ITextureReader<T> TEXTURE_READER;
    private final IMetadataSectionSerializer<TextureMetadataSection> TEXTURE_SERIALIZER;
    private final IMetadataSectionSerializer<AnimationMetadataSection> ANIMATION_SERIALIZER;
    private final Logger LOGGER;

    private IResourceManager resourceManager;

    /**
     * Creates a TextureReloadListener.
     * @param folders               folders to search for animated textures (exclude folders animated by default)
     * @param texReader             reads animated textures
     * @param texLoader             uploads animated textures to the game's texture manager
     * @param texSerializer         serializer for blur and clamp data
     * @param animationSerializer   serializer for animation data
     * @param logger                logs listener-related messages to the game's output
     */
    public TextureReloadListener(String[] folders,
                                 ITextureReader<T>  texReader,
                                 BiConsumer<ResourceLocation, Texture> texLoader,
                                 IMetadataSectionSerializer<TextureMetadataSection> texSerializer,
                                 IMetadataSectionSerializer<AnimationMetadataSection> animationSerializer,
                                 Logger logger) {
        FOLDERS = folders;
        TEXTURE_READER = texReader;
        TEXTURE_LOADER = texLoader;
        TEXTURE_SERIALIZER = texSerializer;
        ANIMATION_SERIALIZER = animationSerializer;
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
            InputStream stream = iresource.getInputStream();

            AnimationMetadataSection animationMetadata = iresource.getMetadata(ANIMATION_SERIALIZER);
            if (animationMetadata == null) {
                return;
            }

            TextureMetadataSection textureMetadata = iresource.getMetadata(TEXTURE_SERIALIZER);
            if (textureMetadata == null) {
                textureMetadata = new TextureMetadataSection(false, false);
            }

            T texture = TEXTURE_READER.read(stream, textureMetadata, animationMetadata);
            TEXTURE_LOADER.accept(resourceLocation, texture);
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to parse animation metadata from {} : {}",
                    resourceLocation, runtimeException);
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {} : {}",
                    resourceLocation, ioException);
        }
    }

}
