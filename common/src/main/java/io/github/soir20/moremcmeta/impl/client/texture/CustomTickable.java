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

/**
 * A custom copy of Minecraft's {@link net.minecraft.client.renderer.texture.Tickable} interface
 * to prevent textures from being ticked by Minecraft's texture manager. Minecraft's texture
 * manager does not remove a texture from the tickable list when that texture is removed,
 * causing OpenGL errors. (Forge patches this bug, but we need to work around it to be
 * multi-platform.)
 */
public interface CustomTickable {

    /**
     * Updates this item on tick.
     */
    void tick();

}
