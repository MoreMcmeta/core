package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.renderer.texture.MockAnimatedTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.MockTextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureReloadListener}. We assume that the retrieval of files with the correct extension
 * works because that is part of Minecraft's code.
 * @author soir20
 */
public class TextureReloadListenerTest {
    private final Logger LOGGER = LogManager.getLogger();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_TextureFactoryNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener(null, mockTextureManager, LOGGER);
    }

    @Test
    public void construct_TextureManagerNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new, null, LOGGER);
    }

    @Test
    public void construct_LoggerNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new, mockTextureManager, null);
    }

    @Test
    public void onResourceManagerReload_ResourceManagerNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(null);
    }

    @Test
    public void onResourceManagerReload_ValidLocations_ManagerHasAllTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_ValidLocationsDuplicates_ManagerHasNoDuplicate() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "bat.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_FilteredLocations_ManagerHasFilteredTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper", "zombie.jpg", "ocelot.png"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingTextureLocations_ManagerHasNoMissingTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png", "zombie.png"), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingMetadataLocations_ManagerHasNoMissingTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png.moremcmeta", "zombie.png.moremcmeta"), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(1, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
    }

    @Test
    public void onResourceManagerReload_TextureAndMetadataInDifferentPacks_ManagerSkipsSeparatedTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), true
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_ResourceLocationException_ManagerCleared() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "bad location.png.moremcmeta", "fol der/ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_PreviouslyLoadedTextures_OldDeletedNewLoaded() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/dolphin.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/ocelot.png")));
    }

    @Test
    public void onResourceManagerReload_ThirdReloadNoLongerAnimatedTextures_NonAnimatedNotDeleted() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerThirdReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "cat.png.moremcmeta", "bear.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        mockTextureManager.loadTexture(new ResourceLocation("textures/creeper.png"),
                new MockAnimatedTexture());
        mockTextureManager.loadTexture(new ResourceLocation("textures/zombie.png"),
                new MockAnimatedTexture());

        listener.onResourceManagerReload(mockResourceManagerThirdReload);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(5, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/cat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/bear.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_DiffNamespaces_AllLoaded() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("test:bat.png.moremcmeta", "moremcmeta:creeper.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener((texStream, metadataStream) -> MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("test", "textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("moremcmeta", "textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }
}