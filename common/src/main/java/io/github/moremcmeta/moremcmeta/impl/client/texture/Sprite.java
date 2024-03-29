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

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}
 * to provide a cleaner interface and make it easier to instantiate in test code.
 * @author soir20
 */
public interface Sprite {

    /**
     * Gets the full path of the atlas that this sprite is stitched to.
     * @return location of atlas this sprite is stitched to
     */
    ResourceLocation atlas();

    /**
     * Gets the position of the sprite's top-left corner on its atlas.
     * @return the sprite's upload point
     */
    long uploadPoint();

    /**
     * Gets the mipmap level of the sprite.
     * @return the mipmap level of the sprite
     */
    int mipmapLevel();

    /**
     * X-coordinate of the top-left corner of this sprite if it is within another sprite.
     * @return x-coordinate of the top-left corner of this sprite if it is within another sprite
     */
    int xOffsetLeft();

    /**
     * Y-coordinate of the top-left corner of this sprite if it is within another sprite.
     * @return y-coordinate of the top-left corner of this sprite if it is within another sprite
     */
    int yOffsetLeft();

    /**
     * X-coordinate of the bottom-right corner of this sprite if it is within another sprite.
     * @return x-coordinate of the bottom-right corner of this sprite if it is within another sprite
     */
    int xOffsetRight();

    /**
     * Y-coordinate of the bottom-right corner of this sprite if it is within another sprite.
     * @return y-coordinate of the bottom-right corner of this sprite if it is within another sprite
     */
    int yOffsetRight();

}
