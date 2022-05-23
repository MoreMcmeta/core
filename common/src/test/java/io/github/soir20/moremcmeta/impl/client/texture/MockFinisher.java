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

package io.github.soir20.moremcmeta.impl.client.texture;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A fake {@link Finisher} that turns items to textures.
 * @param <I> input type
 * @author soir20
 */
public class MockFinisher<I> implements Finisher<I, MockAnimatedTexture> {
    private final Map<ResourceLocation, I> ITEMS;

    public MockFinisher() {
        ITEMS = new HashMap<>();
    }

    @Override
    public void queue(ResourceLocation location, I input) {
        ITEMS.put(location, input);
    }

    @Override
    public Map<ResourceLocation, MockAnimatedTexture> finish() {
        return ITEMS.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> new MockAnimatedTexture())
        );
    }

}
