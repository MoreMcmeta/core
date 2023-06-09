/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
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

package io.github.moremcmeta.moremcmeta.impl.client.adapter;

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.impl.client.resource.MockResourceManager;
import io.github.moremcmeta.moremcmeta.impl.client.texture.MockAnimatedTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureManagerAdapter}.
 * @author soir20
 */
@SuppressWarnings("resource")
public final class TextureManagerAdapterTest {
    private static final ResourceManager MOCK_RESOURCE_MANAGER =
            new MockResourceManager(ImmutableList.of(), ImmutableList.of(), false);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullGetter_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureManagerAdapter(null, (manager, location) -> {});
    }

    @Test
    public void construct_NullAction_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        expectedException.expect(NullPointerException.class);
        new TextureManagerAdapter(() -> texManager, null);
    }

    @Test
    public void register_NullManagerSupplied_NullPointerException() {
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> null, (manager, location) -> {});
        expectedException.expect(NullPointerException.class);
        adapter.register(new ResourceLocation("dummy.png"), new MockAnimatedTexture());
    }

    @Test
    public void register_NullLocation_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});
        expectedException.expect(NullPointerException.class);
        adapter.register(null, new MockAnimatedTexture());
    }

    @Test
    public void register_NullTexture_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});
        expectedException.expect(NullPointerException.class);
        adapter.register(new ResourceLocation("dummy.png"), null);
    }

    @Test
    public void register_LocationAndTextureProvided_TextureRegistered() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});

        AbstractTexture texture = new MockAnimatedTexture();
        adapter.register(new ResourceLocation("dummy.png"), texture);
        assertEquals(texture, texManager.getTexture(new ResourceLocation("dummy.png")));
    }

    @Test
    public void unregister_NullManagerSupplied_NullPointerException() {
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> null,
                (manager, location) -> {});

        expectedException.expect(NullPointerException.class);
        adapter.unregister(new ResourceLocation("dummy.png"));
    }

    @Test
    public void unregister_NullLocation_NullPointerException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});
        expectedException.expect(NullPointerException.class);
        adapter.unregister(null);
    }

    @Test
    public void unregister_NoTexturePresent_UnregisterActionUsed() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        final boolean[] wasUnregistered = {false};
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager,
                (manager, location) -> wasUnregistered[0] = true);

        adapter.unregister(new ResourceLocation("dummy.png"));
        assertTrue(wasUnregistered[0]);
    }

    @Test
    public void unregister_TexturePresent_UnregisterActionUsed() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        final boolean[] wasUnregistered = {false};
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager,
                (manager, location) -> wasUnregistered[0] = true);
        texManager.register(new ResourceLocation("dummy.png"), new MockAnimatedTexture());

        adapter.unregister(new ResourceLocation("dummy.png"));
        assertTrue(wasUnregistered[0]);
    }

    @Test
    public void tick_NullManagerSupplied_NullPointerException() {
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> null,
                (manager, location) -> {});

        expectedException.expect(NullPointerException.class);
        adapter.tick();
    }

    @Test
    public void tick_NoTexturesPresent_NoException() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});
        adapter.tick();
    }

    @Test
    public void tick_CustomTickableTexturesPresent_TexturesNotTicked() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});

        MockAnimatedTexture texture = new MockAnimatedTexture();
        texManager.register(new ResourceLocation("dummy.png"), texture);

        adapter.tick();
        assertEquals(0, texture.ticks());
    }

    @Test
    public void tick_MinecraftTickableTexturesPresent_TexturesTicked() {
        TextureManager texManager = new TextureManager(MOCK_RESOURCE_MANAGER);
        TextureManagerAdapter adapter = new TextureManagerAdapter(() -> texManager, (manager, location) -> {});

        MockAnimatedTexture texture = new MockTickableAnimatedTexture();
        texManager.register(new ResourceLocation("dummy.png"), texture);

        adapter.tick();
        assertEquals(1, texture.ticks());
    }

    /**
     * Mock class combining {@link AbstractTexture} and {@link Tickable}.
     * @author soir20
     */
    private static final class MockTickableAnimatedTexture extends MockAnimatedTexture implements Tickable {}

}