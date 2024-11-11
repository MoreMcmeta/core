/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2024 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.mixinaccess;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * An atlas of sprites that can be located.
 * @author soir20
 */
public interface LocatableSpriteAtlas {

    /**
     * Updates this atlas's sprite finder.
     */
    void moremcmeta_resetSpriteFinder();

    /**
     * Finds a sprite from UV coordinates.
     * @param centerU   u coordinate of the center of the sprite
     * @param centerV   v coordinate of the center of the sprite
     * @return sprite at the UV coordinates, if any
     */
    Optional<TextureAtlasSprite> moremcmeta_findSprite(float centerU, float centerV);

    /**
     * Queues a sprite for update the next time this atlas's sprites are updated.
     * @param spriteName    name of sprite to update
     */
    void moremcmeta_queueSpriteForUpdate(ResourceLocation spriteName);
}
