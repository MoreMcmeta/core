package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.soir20.moremcmeta.client.renderer.texture.MockAnimatedTexture;
import io.github.soir20.moremcmeta.resource.data.MockMetadataSectionSerializer;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureReloadListener}.
 * @author soir20
 */
public class TextureReloadListenerTest {
    private final Logger LOGGER = LogManager.getLogger();
    private final IMetadataSectionSerializer<TextureMetadataSection> DUMMY_TEX_SERIALIZER =
            new MockMetadataSectionSerializer<>(new TextureMetadataSection(false, false));
    private final IMetadataSectionSerializer<AnimationMetadataSection> DUMMY_ANIM_SERIALIZER =
            new MockMetadataSectionSerializer<>(new AnimationMetadataSection(ImmutableList.of(), 10,
                    10, 10, true));

    @Test
    public void onResourceManagerReload_DifferentResourceType_Nothing() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.MODELS));

        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocationsValidMetadata_ManagerHasAllTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertEquals(3, locations.size());
        assertTrue(locations.stream().allMatch(location -> location.getPath().endsWith(".png")));
    }

    @Test
    public void onResourceManagerReload_FilteredLocationsValidMetadata_ManagerHasFilteredTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper", "zombie.jpg"), ImmutableSet.of(), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertEquals(1, locations.size());
        assertTrue(locations.get(0).getPath().endsWith("bat.png"));
    }

    @Test
    public void onResourceManagerReload_MissingLocationsValidMetadata_ManagerHasNoMissingTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"),
                ImmutableSet.of("creeper.png", "zombie.png"), ImmutableSet.of()
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertEquals(1, locations.size());
        assertTrue(locations.get(0).getPath().endsWith("bat.png"));
    }

    @Test
    public void onResourceManagerReload_ValidLocationsInvalidMetadata_ManagerHasNoInvalidTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(),
                ImmutableSet.of("bat.png", "zombie.png")
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertEquals(1, locations.size());
        assertTrue(locations.get(0).getPath().endsWith("creeper.png"));
    }

    @Test
    public void onResourceManagerReload_ValidLocationsNoMetadata_ManagerHasNoInvalidTextures() {
        List<ResourceLocation> locations = new ArrayList<>();
        IResourceManager mockManager = new MockResourceManager(
                ImmutableSet.of("bat.png", "creeper.png", "zombie.png"), ImmutableSet.of(),
                ImmutableSet.of("bat.png", "creeper.png")
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                (location, texture) -> locations.add(location), DUMMY_TEX_SERIALIZER, DUMMY_ANIM_SERIALIZER, LOGGER);

        listener.onResourceManagerReload(mockManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        assertEquals(1, locations.size());
        assertTrue(locations.get(0).getPath().endsWith("zombie.png"));
    }
}