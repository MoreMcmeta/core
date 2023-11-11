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

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlas}
 * because it is difficult to instantiate in tests.
 * @author soir20
 */
@FunctionalInterface
public interface Atlas {

    /**
     * Gets all sprites from this atlas if it is present.
     * @param location      the full path of the texture
     * @return the sprites at the given location if present
     */
    List<Sprite> sprite(ResourceLocation location);

}
