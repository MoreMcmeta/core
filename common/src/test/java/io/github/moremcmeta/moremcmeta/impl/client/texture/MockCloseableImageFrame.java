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

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.io.FrameReader;

/**
 * A fake {@link CloseableImageFrame}.
 * @author soir20
 */
public class MockCloseableImageFrame extends CloseableImageFrame {
    private final ImmutableList<MockCloseableImage> MIPMAPS;
    private int uploads;
    private Point lastUploadPoint;

    public MockCloseableImageFrame(int width, int height, int layers) {
        this(ImmutableList.of(
                new MockCloseableImage(width, height),
                new MockCloseableImage(width >> 1, height >> 1),
                new MockCloseableImage(width >> 2, height >> 2)
        ), layers);
    }

    public MockCloseableImageFrame(int layers) {
        this(ImmutableList.of(
                new MockCloseableImage(100, 100),
                new MockCloseableImage(50, 50),
                new MockCloseableImage(25, 25)
        ), layers);
    }

    public MockCloseableImageFrame(ImmutableList<MockCloseableImage> mipmaps, int layers) {
        super(new FrameReader.FrameData(mipmaps.get(0).width(), mipmaps.get(0).height(), 0, 0), mipmaps, layers);
        MIPMAPS = mipmaps;
    }

    @Override
    public void uploadAt(Point point) {
        super.uploadAt(point);
        uploads++;
        lastUploadPoint = point;
    }

    public int uploadCount() {
        return uploads;
    }

    public Point lastUploadPoint() {
        return lastUploadPoint;
    }

    public MockCloseableImage mipmap(int mipmap) {
        return MIPMAPS.get(mipmap);
    }

}
