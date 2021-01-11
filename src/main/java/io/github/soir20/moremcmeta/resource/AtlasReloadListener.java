package io.github.soir20.moremcmeta.resource;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.client.renderer.texture.IAnimatedTextureFactory;
import io.github.soir20.moremcmeta.client.renderer.texture.ITextureLoader;
import net.minecraft.client.renderer.texture.*;
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
import java.util.*;
import java.util.function.Predicate;

public class AtlasReloadListener<T extends Texture & ITickable> implements ISelectiveResourceReloadListener {
    private final String[] FOLDERS;
    private final ITextureLoader TEXTURE_LOADER;
    private final IAnimatedTextureFactory<T> TEXTURE_FACTORY;
    private final IMetadataSectionSerializer<AnimationMetadataSection> SERIALIZER;
    private final Logger LOGGER;

    private IResourceManager resourceManager;

    public AtlasReloadListener(String[] folders, ITextureLoader texLoader, IAnimatedTextureFactory<T> texFactory,
                               IMetadataSectionSerializer<AnimationMetadataSection> serializer, Logger logger) {
        FOLDERS = folders;
        TEXTURE_LOADER = texLoader;
        TEXTURE_FACTORY = texFactory;
        SERIALIZER = serializer;
        LOGGER = logger;
    }

    @Override
    public IResourceType getResourceType()
    {
        return VanillaResourceType.MODELS;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onResourceManagerReload(IResourceManager resManager, Predicate<IResourceType> resourcePredicate) {
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

    private void uploadToTexManager(ResourceLocation resourcelocation) {
        try (IResource iresource = resourceManager.getResource(resourcelocation)) {
            NativeImage nativeImage = NativeImage.read(iresource.getInputStream());
            AnimationMetadataSection metadata = iresource.getMetadata(SERIALIZER);

            if (metadata != null) {
                Pair<Integer, Integer> pair = metadata.getSpriteSize(nativeImage.getWidth(),
                        nativeImage.getHeight());

                T texture = TEXTURE_FACTORY.createAnimatedTexture(resourcelocation, pair.getFirst(), pair.getSecond(),
                        metadata, 0, nativeImage);

                TEXTURE_LOADER.loadTexture(resourcelocation, texture);
            }
        } catch (RuntimeException runtimeException) {
            LOGGER.error("Unable to parse animation metadata from {} : {}",
                    resourcelocation, runtimeException);
        } catch (IOException ioException) {
            LOGGER.error("Using missing texture, unable to load {} : {}",
                    resourcelocation, ioException);
        }
    }
}
