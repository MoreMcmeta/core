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

import io.github.moremcmeta.moremcmeta.api.math.Point;
import net.minecraft.resources.ResourceLocation;

/**
 * A fake {@link Sprite}.
 * @author soir20
 */
public final class MockSprite implements Sprite {
    private final long UPLOAD_POINT;
    private final int MIPMAP_LEVEL;
    private final ResourceLocation ATLAS;

    public MockSprite() {
        this(Point.pack(0, 0));
    }

    public MockSprite(long uploadPoint) {
        this(uploadPoint, 2);
    }

    public MockSprite(long uploadPoint, int mipmapLevel) {
        this(uploadPoint, mipmapLevel, new ResourceLocation("textures/atlas/dummy.png"));
    }

    public MockSprite(long uploadPoint, int mipmapLevel, ResourceLocation atlas) {
        UPLOAD_POINT = uploadPoint;
        MIPMAP_LEVEL = mipmapLevel;
        ATLAS = atlas;
    }

    @Override
    public ResourceLocation atlas() {
        return ATLAS;
    }

    @Override
    public long uploadPoint() {
        return UPLOAD_POINT;
    }

    @Override
    public int mipmapLevel() {
        return MIPMAP_LEVEL;
    }

    @Override
    public int xOffsetLeft() {
        return 0;
    }

    @Override
    public int yOffsetLeft() {
        return 0;
    }

    @Override
    public int xOffsetRight() {
        return 0;
    }

    @Override
    public int yOffsetRight() {
        return 0;
    }
}
