package io.github.soir20.moremcmeta.resource;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.renderer.texture.MockAnimatedTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.MockTextureManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureReloadListener}. We assume that the retrieval of files with the correct extension
 * works because that is part of Minecraft's code.
 * @author soir20
 */
public class TextureReloadListenerTest {
    private final Logger LOGGER = LogManager.getLogger();

    @Test
    public void onResourceManagerReload_DifferentResourceType_Nothing() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.MODELS));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocations_ManagerHasAllTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_ValidLocationsDuplicates_ManagerHasNoDuplicate() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "bat.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_FilteredLocations_ManagerHasFilteredTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper", "zombie.jpg", "ocelot.png"),
                ImmutableList.of(), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingTextureLocations_ManagerHasNoMissingTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png", "zombie.png"), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingMetadataLocations_ManagerHasNoMissingTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png.moremcmeta", "zombie.png.moremcmeta"), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_TextureAndMetadataInDifferentPacks_ManagerSkipsSeparatedTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), true
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(0, locations.size());
    }



    @Test
    public void onResourceManagerReloadTwice_PreviouslyLoadedTextures_OldDeletedNewLoaded() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        IResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<MockAnimatedTexture> listener = new TextureReloadListener<>(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));
        listener.onResourceManagerReload(mockResourceManagerSecondReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

        List<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/dolphin.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/ocelot.png")));
    }
}