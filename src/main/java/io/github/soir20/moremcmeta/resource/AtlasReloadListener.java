package io.github.soir20.moremcmeta.resource;

import com.mojang.datafixers.util.Pair;
import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.renderer.texture.AnimatedTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class AtlasReloadListener implements ISelectiveResourceReloadListener {
    private IResourceManager resourceManager;

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

            for (String folder : MoreMcmeta.FOLDERS) {
                Collection<ResourceLocation> folderMetadata = resourceManager.getAllResourceLocations(
                        "textures/" + folder,
                        fileName -> fileName.endsWith(".png")
                );

                folderMetadata.forEach(this::uploadToTexManager);
            }
        }
    }

    private void uploadToTexManager(ResourceLocation resourcelocation) {
        try (IResource iresource = resourceManager.getResource(resourcelocation)) {
            NativeImage nativeImage = NativeImage.read(iresource.getInputStream());

            AnimationMetadataSection metadata = iresource.getMetadata(AnimationMetadataSection.SERIALIZER);
            if (metadata == null) {
                metadata = AnimationMetadataSection.EMPTY;
            }

            Pair<Integer, Integer> pair = metadata.getSpriteSize(nativeImage.getWidth(), nativeImage.getHeight());

            AnimatedTexture texture = new AnimatedTexture(resourcelocation, pair.getFirst(), pair.getSecond(),
                    metadata, 0, nativeImage);

            Minecraft.getInstance().getTextureManager().loadTexture(resourcelocation, texture);
        } catch (RuntimeException runtimeException) {
            MoreMcmeta.LOGGER.error("Unable to parse animation metadata from {} : {}",
                    resourcelocation, runtimeException);
        } catch (IOException ioException) {
            MoreMcmeta.LOGGER.error("Using missing texture, unable to load {} : {}",
                    resourcelocation, ioException);
        }
    }
}
