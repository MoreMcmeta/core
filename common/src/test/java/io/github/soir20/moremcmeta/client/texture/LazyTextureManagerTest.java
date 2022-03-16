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

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.impl.client.texture.CustomTickable;
import io.github.soir20.moremcmeta.impl.client.texture.LazyTextureManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link LazyTextureManager}.
 * @author soir20
 */
public class LazyTextureManagerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullManagerGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new LazyTextureManager<>(null, new MockFinisher<>());
    }

    @Test
    public void construct_NullFinisher_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();

        expectedException.expect(NullPointerException.class);
        new LazyTextureManager<>(texManager, null);
    }

    @Test
    public void register_NullLocation_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.register(null, 1);
    }

    @Test
    public void register_NullBuilder_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.register(new ResourceLocation("bat.png"), null);
    }

    @Test
    public void registerAndTick_TickableInput_UnfinishedNotTicked() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<CustomTickable, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>());

        MockAnimatedTexture tickable = new MockAnimatedTexture();
        wrapper.register(new ResourceLocation("bat.png"), tickable);
        wrapper.tick();

        assertEquals(0, tickable.getTicks());
    }

    @Test
    public void register_ManagerHasTexture_OldUnregistered() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        ResourceLocation location = new ResourceLocation("bat.png");

        texManager.register(location, new MockAnimatedTexture());

        LazyTextureManager<CustomTickable, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>());

        wrapper.register(location, new MockAnimatedTexture());
        assertNull(texManager.getTexture(location));
    }

    @Test
    public void registerAndFinish_MultipleTextures_AllAdded() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, 1);

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, 2);

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, 2);

        wrapper.finishQueued();
        assertTrue(texManager.getTexture(location1) instanceof MockAnimatedTexture);
        assertTrue(texManager.getTexture(location2) instanceof MockAnimatedTexture);
        assertTrue(texManager.getTexture(location3) instanceof MockAnimatedTexture);
    }

    @Test
    public void finish_NoneQueued_NoException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        wrapper.finishQueued();
    }

    @Test
    public void unregister_NullLocation_NullPointerException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.unregister(null);
    }

    @Test
    public void unregister_AddedByWrapper_TextureRemoved() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, 1);

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, 2);

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, 2);

        wrapper.finishQueued();
        wrapper.unregister(location1);

        assertNull(texManager.getTexture(location1));
        assertNotNull(texManager.getTexture(location2));
        assertNotNull(texManager.getTexture(location3));
    }

    @Test
    public void tick_SomeQueuedNoneFinished_NoException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, 1);

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, 2);

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, 2);

        wrapper.tick();
    }

    @Test
    public void tick_NoneQueuedNoneFinished_NoException() {
        MockManager<AbstractTexture> texManager = new MockManager<>();
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                texManager, new MockFinisher<>()
        );

        wrapper.tick();
    }

}