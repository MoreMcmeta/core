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

package io.github.moremcmeta.moremcmeta.api.client.texture;

import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link SpriteName} utility functions.
 * @author soir20
 */
public final class SpriteNameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void fromTexturePath_NullSpriteName_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        SpriteName.fromTexturePath(null);
    }

    @Test
    public void fromTexturePath_NoPrefixNoSuffix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "block/glass/clear"),
                SpriteName.fromTexturePath(new ResourceLocation("test", "block/glass/clear"))
        );
    }

    @Test
    public void fromTexturePath_HasPrefix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "textures/block/glass/clear"),
                SpriteName.fromTexturePath(new ResourceLocation("test", "textures/block/glass/clear"))
        );
    }

    @Test
    public void fromTexturePath_HasSuffix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "block/glass/clear.png"),
                SpriteName.fromTexturePath(new ResourceLocation("test", "block/glass/clear.png"))
        );
    }

    @Test
    public void fromTexturePath_HasPrefixHasSuffix_Converted() {
        assertEquals(
                new ResourceLocation("test", "block/glass/clear"),
                SpriteName.fromTexturePath(new ResourceLocation("test", "textures/block/glass/clear.png"))
        );
    }

    @Test
    public void fromTexturePath_BlankPath_Converted() {
        assertEquals(
                new ResourceLocation("test", "textures/.png"),
                SpriteName.fromTexturePath(new ResourceLocation("test", "textures/.png"))
        );
    }

    @Test
    public void toTexturePath_NullSpriteName_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        SpriteName.toTexturePath(null);
    }

    @Test
    public void toTexturePath_NoPrefixNoSuffix_Converted() {
        assertEquals(
                new ResourceLocation("test", "textures/block/glass/clear.png"),
                SpriteName.toTexturePath(new ResourceLocation("test", "block/glass/clear"))
        );
    }

    @Test
    public void toTexturePath_HasPrefix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "textures/block/glass/clear"),
                SpriteName.toTexturePath(new ResourceLocation("test", "textures/block/glass/clear"))
        );
    }

    @Test
    public void toTexturePath_HasSuffix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "block/glass/clear.png"),
                SpriteName.toTexturePath(new ResourceLocation("test", "block/glass/clear.png"))
        );
    }

    @Test
    public void toTexturePath_HasPrefixHasSuffix_NotConverted() {
        assertEquals(
                new ResourceLocation("test", "textures/block/glass/clear.png"),
                SpriteName.toTexturePath(new ResourceLocation("test", "textures/block/glass/clear.png"))
        );
    }

    @Test
    public void isSpriteName_NullLocation_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        SpriteName.isSpriteName(null);
    }

    @Test
    public void isSpriteName_NoPrefixNoSuffix_True() {
        assertTrue(SpriteName.isSpriteName(new ResourceLocation("test", "block/glass/clear")));
    }

    @Test
    public void isSpriteName_HasPrefix_False() {
        assertFalse(SpriteName.isSpriteName(new ResourceLocation("test", "textures/block/glass/clear")));
    }

    @Test
    public void isSpriteName_HasSuffix_False() {
        assertFalse(SpriteName.isSpriteName(new ResourceLocation("test", "block/glass/clear.png")));
    }

    @Test
    public void isSpriteName_HasPrefixHasSuffix_False() {
        assertFalse(SpriteName.isSpriteName(new ResourceLocation("test", "textures/block/glass/clear.png")));
    }

    @Test
    public void isSpriteName_BlankPath_False() {
        assertFalse(SpriteName.isSpriteName(new ResourceLocation("test", "")));
    }

}