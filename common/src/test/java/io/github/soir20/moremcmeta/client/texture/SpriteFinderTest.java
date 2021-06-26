package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests the {@link SpriteFinder}.
 * @author soir20
 */
public class SpriteFinderTest {
    private static final ResourceLocation LOCATION_BOTH = new ResourceLocation("textures/bat.png");
    private static final ResourceLocation LOCATION_PREFIX = new ResourceLocation("textures/bat");
    private static final ResourceLocation LOCATION_EXTENSION = new ResourceLocation("bat.png");
    private static final ResourceLocation LOCATION_IN_ATLAS = new ResourceLocation("bat");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullAtlasGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SpriteFinder(null);
    }

    @Test
    public void find_GetterReturnsNull_NullPointerException() {
        SpriteFinder finder = new SpriteFinder((location) -> null);

        expectedException.expect(NullPointerException.class);
        finder.findSprite(LOCATION_BOTH);
    }

    @Test
    public void find_NullLocation_NullPointerException() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(LOCATION_IN_ATLAS) ? 
                        Optional.of(new MockAtlasSprite(LOCATION_IN_ATLAS)) : Optional.empty()
        );

        expectedException.expect(NullPointerException.class);
        finder.findSprite(null);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Test
    public void find_NullAtlases_NullPointerException() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> null
        );

        expectedException.expect(NullPointerException.class);
        finder.findSprite(LOCATION_BOTH);
    }

    @Test
    public void find_NameWithPrefixAndExtension_SpriteFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(LOCATION_IN_ATLAS) ? 
                        Optional.of(new MockAtlasSprite(LOCATION_IN_ATLAS)) : Optional.empty()
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_BOTH);
        assertTrue(result.isPresent());
        assertEquals(LOCATION_IN_ATLAS, result.get().getName());
    }

    @Test
    public void find_NameWithoutPrefixAndExtension_SpriteFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(LOCATION_IN_ATLAS) ? 
                        Optional.of(new MockAtlasSprite(LOCATION_IN_ATLAS)) : Optional.empty()
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_BOTH);
        assertTrue(result.isPresent());
        assertEquals(LOCATION_IN_ATLAS, result.get().getName());
    }

    @Test
    public void find_NameWithExtension_SpriteFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(LOCATION_IN_ATLAS) ? 
                        Optional.of(new MockAtlasSprite(LOCATION_IN_ATLAS)) : Optional.empty()
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_EXTENSION);
        assertTrue(result.isPresent());
        assertEquals(LOCATION_IN_ATLAS, result.get().getName());
    }

    @Test
    public void find_NameWithPrefix_SpriteFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(LOCATION_IN_ATLAS) ? 
                        Optional.of(new MockAtlasSprite(LOCATION_IN_ATLAS)) : Optional.empty()
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_PREFIX);
        assertTrue(result.isPresent());
        assertEquals(LOCATION_IN_ATLAS, result.get().getName());
    }

    @Test
    public void find_SpriteNotPresent_SpriteNotFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> Optional.empty()
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_BOTH);
        assertFalse(result.isPresent());
    }

    @Test
    public void find_SpriteHasMissingLocation_SpriteNotFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) ->
                        Optional.of(new MockAtlasSprite(MissingTextureAtlasSprite.getLocation()))
        );

        Optional<ISprite> result = finder.findSprite(LOCATION_BOTH);
        assertFalse(result.isPresent());
    }

}