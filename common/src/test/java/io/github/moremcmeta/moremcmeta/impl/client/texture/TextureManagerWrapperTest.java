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

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureManagerWrapper}.
 * @author soir20
 */
public final class TextureManagerWrapperTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullManagerGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureManagerWrapper<>(null);
    }

    @Test
    public void register_NullLocation_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        expectedException.expect(NullPointerException.class);
        wrapper.register(null, new MockAnimatedTexture());
    }

    @Test
    public void register_NullBuilder_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        expectedException.expect(NullPointerException.class);
        wrapper.register(new ResourceLocation("bat.png"), null);
    }

    @Test
    public void register_ManagerHasTexture_OldUnregistered() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        ResourceLocation location = new ResourceLocation("bat.png");

        texManager.register(location, new MockAnimatedTexture());

        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        MockAnimatedTexture expectedTexture = new MockAnimatedTexture();
        wrapper.register(location, expectedTexture);
        assertEquals(expectedTexture, texManager.texture(location));
    }

    @Test
    public void register_MultipleTextures_AllAdded() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, new MockAnimatedTexture());

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, new MockAnimatedTexture());

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, new MockAnimatedTexture());

        assertTrue(texManager.texture(location1) instanceof MockAnimatedTexture);
        assertTrue(texManager.texture(location2) instanceof MockAnimatedTexture);
        assertTrue(texManager.texture(location3) instanceof MockAnimatedTexture);
    }

    @Test
    public void unregister_NullLocation_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        expectedException.expect(NullPointerException.class);
        wrapper.unregister(null);
    }

    @Test
    public void unregister_AddedByWrapper_TextureRemoved() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, new MockAnimatedTexture());

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, new MockAnimatedTexture());

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, new MockAnimatedTexture());

        wrapper.unregister(location1);

        assertNull(texManager.texture(location1));
        assertNotNull(texManager.texture(location2));
        assertNotNull(texManager.texture(location3));
    }

    @Test
    public void tick_SeveralRegistered_AllTicked() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        ResourceLocation location1 = new ResourceLocation("bat.png");
        MockAnimatedTexture texture1 = new MockAnimatedTexture();
        wrapper.register(location1, texture1);

        ResourceLocation location2 = new ResourceLocation("cat.png");
        MockAnimatedTexture texture2 = new MockAnimatedTexture();
        wrapper.register(location2, texture2);

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        MockAnimatedTexture texture3 = new MockAnimatedTexture();
        wrapper.register(location3, texture3);

        wrapper.tick();

        assertEquals(1, texture1.ticks());
        assertEquals(1, texture2.ticks());
        assertEquals(1, texture3.ticks());
    }

    @Test
    public void tick_NoneRegistered_NoException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        TextureManagerWrapper<MockAnimatedTexture> wrapper = new TextureManagerWrapper<>(texManager);

        wrapper.tick();
    }

}