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

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @param <R> resource type
 * @author soir20
 */
public final class MockManager<R> implements Manager<R> {
    private final Map<ResourceLocation, R> TEXTURES;
    private final Map<ResourceLocation, Tickable> TICKABLE_TEXTURES;

    public MockManager() {
        TEXTURES = new HashMap<>();
        TICKABLE_TEXTURES = new HashMap<>();
    }

    @Override
    public void register(ResourceLocation textureLocation, R textureObj) {
        TEXTURES.put(textureLocation, textureObj);
        TICKABLE_TEXTURES.remove(textureLocation);
        if (textureObj instanceof Tickable) {
            TICKABLE_TEXTURES.put(textureLocation, (Tickable) textureObj);
        }
    }

    @Override
    public void unregister(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public void tick() {
        TICKABLE_TEXTURES.values().forEach(Tickable::tick);
    }

    public R texture(ResourceLocation location) {
        return TEXTURES.get(location);
    }
}
