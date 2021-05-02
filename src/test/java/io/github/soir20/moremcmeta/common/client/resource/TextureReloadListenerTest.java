package io.github.soir20.moremcmeta.common.client.resource;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.common.client.renderer.texture.MockAnimatedTexture;
import io.github.soir20.moremcmeta.common.client.renderer.texture.MockTextureManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.VanillaResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

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
    @SuppressWarnings("ConstantConditions")
    public void construct_TextureFactoryNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener(null, mockTextureManager, LOGGER);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_TextureManagerNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureReloadListener(MockAnimatedTexture::new, null, LOGGER);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void construct_LoggerNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener(MockAnimatedTexture::new, mockTextureManager, null);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void onResourceManagerReload_ResourceManagerNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(null, (type) -> type.equals(VanillaResourceType.TEXTURES));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void onResourceManagerReload_PredicateNull_NullPointerException() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(mockResourceManager, null);
    }

    @Test
    public void onResourceManagerReload_DifferentResourceType_Nothing() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.MODELS));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertTrue(locations.isEmpty());
    }

    @Test
    public void onResourceManagerReload_ValidLocations_ManagerHasAllTextures() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
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

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
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

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
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

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
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

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
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

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager, (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_ResourceLocationException_ManagerCleared() {
        MockTextureManager mockTextureManager = new MockTextureManager();

        IResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        IResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "bad location.png.moremcmeta", "fol der/ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));
        listener.onResourceManagerReload(mockResourceManagerSecondReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_PreviouslyLoadedTextures_OldDeletedNewLoaded() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        IResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));
        listener.onResourceManagerReload(mockResourceManagerSecondReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/dolphin.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/ocelot.png")));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void onResourceManagerReload_ThirdReloadNoLongerAnimatedTextures_NonAnimatedNotDeleted() {
        MockTextureManager mockTextureManager = new MockTextureManager();
        IResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        IResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );
        IResourceManager mockResourceManagerThirdReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "cat.png.moremcmeta", "bear.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));
        listener.onResourceManagerReload(mockResourceManagerSecondReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

        mockTextureManager.loadTexture(new ResourceLocation("textures/creeper.png"),
                new MockAnimatedTexture(null, null));
        mockTextureManager.loadTexture(new ResourceLocation("textures/zombie.png"),
                new MockAnimatedTexture(null, null));

        listener.onResourceManagerReload(mockResourceManagerThirdReload,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

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
        IResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("test:bat.png.moremcmeta", "moremcmeta:creeper.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener listener = new TextureReloadListener(MockAnimatedTexture::new,
                mockTextureManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager,
                (type) -> type.equals(VanillaResourceType.TEXTURES));

        Set<ResourceLocation> locations = mockTextureManager.getLocations();
        assertEquals(3, locations.size());
        assertTrue(locations.contains(new ResourceLocation("test", "textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("moremcmeta", "textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
    }
}