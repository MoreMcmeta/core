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

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.impl.client.io.FrameReader;
import io.github.soir20.moremcmeta.api.math.Point;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImage;
import io.github.soir20.moremcmeta.impl.client.texture.CloseableImageFrame;

/**
 * A fake {@link CloseableImageFrame}.
 * @author soir20
 */
public class MockCloseableImageFrame extends CloseableImageFrame {
    private final int FRAME_NUMBER;
    private int uploads;

    public MockCloseableImageFrame() {
        this(0);
    }

    public MockCloseableImageFrame(int frameNumber) {
        super(new FrameReader.FrameData(10, 10, 0, 0, 1),
                ImmutableList.of(new MockCloseableImage(), new MockCloseableImage(), new MockCloseableImage()),
                new SharedMipmapLevel(2));
        FRAME_NUMBER = frameNumber;
    }

    public MockCloseableImageFrame(int width, int height) {
        this(width, height, 3);
    }

    public MockCloseableImageFrame(int width, int height, int mipmap) {
        super(new FrameReader.FrameData(width, height, 0, 0, 1), createMipmaps(mipmap, width, height),
                new SharedMipmapLevel(mipmap));
        FRAME_NUMBER = 0;
    }

    @Override
    public void uploadAt(Point point) {
        super.uploadAt(point);
        uploads++;
    }

    public int getFrameNumber() {
        return FRAME_NUMBER;
    }

    public int getUploadCount() {
        return uploads;
    }

    private static ImmutableList<CloseableImage> createMipmaps(int mipmap, int width, int height) {
        ImmutableList.Builder<CloseableImage> builder = new ImmutableList.Builder<>();

        for (int level = 0; level <= mipmap; level++) {
            builder.add(new MockCloseableImage(width >> level, height >> level));
        }

        return builder.build();
    }

}
