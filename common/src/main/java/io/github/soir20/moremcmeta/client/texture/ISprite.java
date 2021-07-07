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

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.resources.ResourceLocation;

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}
 * to provide a cleaner interface and make it easier to instantiate in test code.
 * @author soir20
 */
public interface ISprite {

    /**
     * Binds the sprite (and thus its atlas) to OpenGL.
     */
    void bind();

    /**
     * Gets the name of this sprite (without an extension).
     * @return the sprite's name
     */
    ResourceLocation getName();

    /**
     * Gets the position of the sprite's top-left corner on its atlas.
     * @return the sprite's upload point
     */
    Point getUploadPoint();

}
