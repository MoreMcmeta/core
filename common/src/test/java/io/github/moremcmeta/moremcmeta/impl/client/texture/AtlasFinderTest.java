/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link AtlasFinder}.
 * @author soir20
 */
public final class AtlasFinderTest {
    public static final ImmutableSet<ResourceLocation> ATLAS_LOCATIONS = ImmutableSet.of(
            ResourceLocation.parse("textures/atlas/blocks.png"),
            ResourceLocation.parse("textures/atlas/signs.png"),
            ResourceLocation.parse("textures/atlas/banner_patterns.png"),
            ResourceLocation.parse("textures/atlas/shield_patterns.png"),
            ResourceLocation.parse("textures/atlas/chest.png"),
            ResourceLocation.parse("textures/atlas/beds.png"),
            ResourceLocation.parse("textures/atlas/particles.png"),
            ResourceLocation.parse("textures/atlas/paintings.png"),
            ResourceLocation.parse("textures/atlas/mob_effects.png")
    );
    private static final ResourceLocation TEST_LOCATION = ResourceLocation.parse("textures/bat.png");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullAtlasGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AtlasFinder(null, ATLAS_LOCATIONS);
    }


    @Test
    public void find_NullLocationSet_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AtlasFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                null
        );
    }
    @Test
    public void find_GetterReturnsNull_NullPointerException() {
        AtlasFinder finder = new AtlasFinder((location) -> null, ATLAS_LOCATIONS);

        expectedException.expect(NullPointerException.class);
        finder.findSprites(TEST_LOCATION);
    }

    @Test
    public void find_NullLocation_NullPointerException() {
        AtlasFinder finder = new AtlasFinder(
                (atlasLocation) -> (spriteLocation) -> spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        expectedException.expect(NullPointerException.class);
        finder.findSprites(null);
    }

    @Test
    public void find_NullAtlases_NullPointerException() {
        AtlasFinder finder = new AtlasFinder(
                (atlasLocation) -> (spriteLocation) -> null,
                ATLAS_LOCATIONS
        );

        expectedException.expect(NullPointerException.class);
        finder.findSprites(TEST_LOCATION);
    }

    @Test
    public void find_SpriteNotPresent_SpriteNotFound() {
        AtlasFinder finder = new AtlasFinder(
                (atlasLocation) -> (spriteLocation) -> ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertTrue(result.isEmpty());
    }

    @Test
    public void find_SpriteHasMissingLocation_SpriteFound() {
        AtlasFinder finder = new AtlasFinder(
                (atlasLocation) -> (spriteLocation) ->
                        ImmutableList.of(new MockSprite()),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(ATLAS_LOCATIONS.size(), result.size());
    }

    @Test
    public void find_SpriteInBlockAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/blocks.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInSignAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/signs.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInBannerAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/banner_patterns.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInShieldAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/shield_patterns.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInChestAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/chest.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInBedAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/beds.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInParticleAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/particles.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInPaintingAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/paintings.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

    @Test
    public void find_SpriteInEffectAtlas_SpriteFound() {
        AtlasFinder finder = new AtlasFinder((atlasLocation) -> (spriteLocation) ->
                atlasLocation.equals(ResourceLocation.parse("textures/atlas/mob_effects.png"))
                        && spriteLocation.equals(TEST_LOCATION) ?
                        ImmutableList.of(new MockSprite()) : ImmutableList.of(),
                ATLAS_LOCATIONS
        );

        List<Sprite> result = finder.findSprites(TEST_LOCATION);
        assertEquals(1, result.size());
    }

}