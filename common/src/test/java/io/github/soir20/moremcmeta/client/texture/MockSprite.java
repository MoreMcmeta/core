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

package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.resources.ResourceLocation;

/**
 * A fake {@link Sprite}.
 * @author soir20
 */
public class MockSprite implements Sprite {
    private final ResourceLocation NAME;
    private final Point UPLOAD_POINT;
    private final int MIPMAP_LEVEL;
    private int timesBound;

    public MockSprite(ResourceLocation name) {
        this(name, new Point(0, 0));
    }

    public MockSprite(ResourceLocation name, Point uploadPoint) {
        this(name, uploadPoint, 2);
    }

    public MockSprite(Point uploadPoint) {
        this(new ResourceLocation("dummy"), uploadPoint);
    }

    public MockSprite(int mipmapLevel) {
        this(new ResourceLocation("dummy"), new Point(0, 0), mipmapLevel);
    }

    public MockSprite(ResourceLocation name, Point uploadPoint, int mipmapLevel) {
        NAME = name;
        UPLOAD_POINT = uploadPoint;
        MIPMAP_LEVEL = mipmapLevel;
    }

    @Override
    public void bind() {
        timesBound++;
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public Point getUploadPoint() {
        return UPLOAD_POINT;
    }

    @Override
    public int getMipmapLevel() {
        return MIPMAP_LEVEL;
    }

    public int getBindCount() {
        return timesBound;
    }
}
