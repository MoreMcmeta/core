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

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A fake tickable texture.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockAnimatedTexture extends AbstractTexture implements CustomTickable {
    private int ticks;

    @Override
    public void tick() {
        ticks++;
    }

    public int ticks() {
        return ticks;
    }

    @Override
    public void load(ResourceManager resourceManager) {}

}
