package io.github.soir20.moremcmeta.resource;

import io.github.soir20.moremcmeta.MoreMcmeta;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            Set<String> folders = MoreMcmeta.ATLASES.keySet();

            for (String folder : folders) {
                Collection<ResourceLocation> folderMetadata = resourceManager.getAllResourceLocations(
                        "textures/" + folder,
                        fileName -> fileName.endsWith(".png")
                );

                Map<AtlasTexture, List<ResourceLocation>> uploadGroups = folderMetadata.stream().distinct().collect(
                        Collectors.groupingBy(MoreMcmeta.ATLASES.get(folder))
                );

                uploadGroups.forEach(this::uploadToTexManager);
            }
        }
    }

    private void uploadToTexManager(AtlasTexture atlas, Collection<ResourceLocation> textures) {
        TextureManager texManager = Minecraft.getInstance().getTextureManager();
        IProfiler profiler = Minecraft.getInstance().getProfiler();

        Stream<ResourceLocation> spriteLocations = textures.stream().map(
                texture -> {
                    String spritePath = removeExtension(texture.getPath()).replace("textures/", "");
                    ResourceLocation spriteLocation = new ResourceLocation(texture.getNamespace(), spritePath);

                    MoreMcmeta.LOGGER.info("Uploading animated texture for {} to texture manager", spritePath);
                    texManager.loadTexture(texture, atlas);

                    return spriteLocation;
                }
        );

        AtlasTexture.SheetData sheetData = atlas.stitch(resourceManager, spriteLocations, profiler,
                Minecraft.getInstance().gameSettings.mipmapLevels);
        atlas.upload(sheetData);
    }

    private static String removeExtension(String path) {
        int extStart = path.lastIndexOf('.');
        return path.substring(0, extStart);
    }

}
