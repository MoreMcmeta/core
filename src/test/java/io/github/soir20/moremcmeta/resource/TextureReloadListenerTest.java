package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.client.renderer.texture.MockAnimatedTexture;
import io.github.soir20.moremcmeta.resource.data.MockAnimationMetadataSectionSerializer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TextureReloadListenerTest {
    private final String[] FOLDERS = {"entity", "gui", "map"};
    private final Logger LOGGER = LogManager.getLogger();
    private final AnimationMetadataSection DUMMY_METADATA = new AnimationMetadataSection(ImmutableList.of(),
            10, 10, 10, true);

    @Test
    public void onResourceManagerReload_DifferentResourceType_Nothing() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> locations.add(location), MockAnimatedTexture::new,
                this::createNativeImage, new MockAnimationMetadataSectionSerializer(DUMMY_METADATA), LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.MODELS));

        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocationsValidMetadata_ManagerHasAllTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        List<Texture> textures = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> { locations.add(location); textures.add(texture); },
                MockAnimatedTexture::new, this::createNativeImage,
                new MockAnimationMetadataSectionSerializer(DUMMY_METADATA), LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertTrue(locations.stream().allMatch(location -> location.getPath().endsWith(".png")));
        assertEquals(9, textures.size());
    }

    @Test
    public void onResourceManagerReload_FilteredLocationsValidMetadata_ManagerHasFilteredTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper", "zombie.jpg"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> locations.add(location),
                MockAnimatedTexture::new, this::createNativeImage,
                new MockAnimationMetadataSectionSerializer(DUMMY_METADATA), LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertTrue(locations.stream().allMatch(location -> location.getPath().endsWith("bat.png")));
        assertEquals(3, locations.size());
    }

    @Test
    public void onResourceManagerReload_MissingLocationsValidMetadata_ManagerHasNoMissingTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"),
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of()

        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> locations.add(location), MockAnimatedTexture::new,
                this::createNativeImage, new MockAnimationMetadataSectionSerializer(null),
                LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocationsInvalidMetadata_ManagerHasNoInvalidTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(),
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png")
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> locations.add(location), MockAnimatedTexture::new,
                this::createNativeImage, new MockAnimationMetadataSectionSerializer(DUMMY_METADATA),
                LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocationsNoMetadata_ManagerHasNoInvalidTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(),
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png")
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(
                FOLDERS, (location, texture) -> locations.add(location), MockAnimatedTexture::new,
                this::createNativeImage, new MockAnimationMetadataSectionSerializer(null),
                LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertTrue(locations.isEmpty());
    }

    private NativeImage createNativeImage(InputStream inputStreamIn) {
        return new NativeImage(10, 10, false);
    }
}