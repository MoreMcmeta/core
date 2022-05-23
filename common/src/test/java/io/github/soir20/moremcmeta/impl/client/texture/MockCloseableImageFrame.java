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

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.io.FrameReader;

/**
 * A fake {@link CloseableImageFrame}.
 * @author soir20
 */
public class MockCloseableImageFrame extends CloseableImageFrame {
    private final ImmutableList<MockCloseableImage> MIPMAPS;
    private int uploads;

    public MockCloseableImageFrame() {
        this(ImmutableList.of(
                new MockCloseableImage(100, 100),
                new MockCloseableImage(50, 50),
                new MockCloseableImage(25, 25)
        ));
    }

    private MockCloseableImageFrame(ImmutableList<MockCloseableImage> mipmaps) {
        super(new FrameReader.FrameData(100, 100, 0, 0), mipmaps);
        MIPMAPS = mipmaps;
    }

    @Override
    public void uploadAt(Point point) {
        super.uploadAt(point);
        uploads++;
    }

    public int getUploadCount() {
        return uploads;
    }

    public MockCloseableImage getMipmap(int mipmap) {
        return MIPMAPS.get(mipmap);
    }

}
