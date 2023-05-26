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

import net.minecraft.resources.ResourceLocation;

/**
 * An adapter for {@link net.minecraft.client.renderer.texture.TextureAtlasSprite}
 * to provide a cleaner interface and make it easier to instantiate in test code.
 * @author soir20
 */
public interface Sprite {

    /**
     * Gets the name of this sprite (without an extension).
     * @return the sprite's name
     */
    ResourceLocation name();

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
     * Gets the width of the sprite.
     * @return the width of the sprite
     */
    int width();

    /**
     * Gets the height of the sprite.
     * @return the height of the sprite
     */
    int height();

}
