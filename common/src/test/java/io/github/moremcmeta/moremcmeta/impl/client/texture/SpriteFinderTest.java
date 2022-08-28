/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moremcmeta.moremcmeta.impl.client.texture;

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
    private static final ResourceLocation TEST_LOCATION = new ResourceLocation("textures/bat.png");

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
        finder.findSprite(TEST_LOCATION);
    }

    @Test
    public void find_NullLocation_NullPointerException() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
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
        finder.findSprite(TEST_LOCATION);
    }

    @Test
    public void find_SpriteNotPresent_SpriteNotFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) -> Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertFalse(result.isPresent());
    }

    @Test
    public void find_SpriteHasMissingLocation_SpriteFound() {
        SpriteFinder finder = new SpriteFinder(
                (atlasLocation) -> (spriteLocation) ->
                        Optional.of(new MockSprite(MissingTextureAtlasSprite.getLocation()))
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInBlockAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/blocks.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInSignAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/signs.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInBannerAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/banner_patterns.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInShieldAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/shield_patterns.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInChestAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/chest.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInBedAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/beds.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInParticleAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/particles.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInPaintingAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/paintings.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

    @Test
    public void find_SpriteInEffectAtlas_SpriteFound() {
        SpriteFinder finder = new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(new ResourceLocation("textures/atlas/mob_effects.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        Optional.of(new MockSprite(TEST_LOCATION)) : Optional.empty()
        );

        Optional<Sprite> result = finder.findSprite(TEST_LOCATION);
        assertTrue(result.isPresent());
    }

}