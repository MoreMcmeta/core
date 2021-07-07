/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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

import io.github.soir20.moremcmeta.client.resource.MockResourceManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests the {@link LazyTextureManager}.
 * @author soir20
 */
public class LazyTextureManagerTest {
    private static final ResourceManager MOCK_RESOURCE_MANAGER = new MockResourceManager(Collections.emptyList(),
            Collections.emptyList(), false);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullManagerGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new LazyTextureManager<>(null, new MockFinisher<>());
    }

    @Test
    public void construct_NullFinisher_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);

        expectedException.expect(NullPointerException.class);
        new LazyTextureManager<>(() -> texManager, null);
    }

    @Test
    public void register_NullLocation_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.register(null, 1);
    }

    @Test
    public void register_NullBuilder_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.register(new ResourceLocation("bat.png"), null);
    }

    @Test
    public void registerAndTick_TickableInput_UnfinishedNotTicked() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<CustomTickable, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>());

        MockAnimatedTexture tickable = new MockAnimatedTexture();
        wrapper.register(new ResourceLocation("bat.png"), tickable);
        wrapper.tick();

        assertEquals(0, tickable.getTicks());
    }

    @Test
    public void register_ManagerHasTexture_OldUnregisteredOnRenderThread() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        ResourceLocation location = new ResourceLocation("bat.png");

        texManager.register(location, new MockAnimatedTexture());

        LazyTextureManager<CustomTickable, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>());

        wrapper.register(location, new MockAnimatedTexture());

        // We aren't on the render thread, so this texture should not null
        assertNotNull(texManager.getTexture(location));

    }

    @Test
    public void registerAndFinish_MultipleTextures_AllAdded() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
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
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        wrapper.finishQueued();
    }

    @Test
    public void unregister_NullLocation_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        expectedException.expect(NullPointerException.class);
        wrapper.unregister(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void unregister_AddedByWrapper_TextureRemoved() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        ResourceLocation location1 = new ResourceLocation("bat.png");
        wrapper.register(location1, 1);

        ResourceLocation location2 = new ResourceLocation("cat.png");
        wrapper.register(location2, 2);

        ResourceLocation location3 = new ResourceLocation("ocelot.png");
        wrapper.register(location3, 2);

        wrapper.finishQueued();
        wrapper.unregister(location1);

        /* Textures can only be removed from Minecraft's manager on the render thread,
           so we determine if they are removed based on whether they still get ticked. */
        wrapper.tick();

        assertEquals(0, ((MockAnimatedTexture) texManager.getTexture(location1)).getTicks());
        assertEquals(1, ((MockAnimatedTexture) texManager.getTexture(location2)).getTicks());
        assertEquals(1, ((MockAnimatedTexture) texManager.getTexture(location3)).getTicks());
    }

    @Test
    public void tick_SomeQueuedNoneFinished_NoException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
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
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        LazyTextureManager<Integer, MockAnimatedTexture> wrapper = new LazyTextureManager<>(
                () -> texManager, new MockFinisher<>()
        );

        wrapper.tick();
    }

}