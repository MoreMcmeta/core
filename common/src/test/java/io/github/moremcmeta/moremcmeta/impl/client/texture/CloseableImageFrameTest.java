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
import io.github.moremcmeta.moremcmeta.api.client.texture.Color;
import io.github.moremcmeta.moremcmeta.api.client.texture.PixelOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.math.Area;
import io.github.moremcmeta.moremcmeta.api.math.Point;
import io.github.moremcmeta.moremcmeta.impl.client.io.FrameReader;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CloseableImageFrame}.
 * @author soir20
 */
public final class CloseableImageFrameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_FrameDataNull_NullPointerException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(new MockCloseableImage());

        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(null, mipmaps, 1);
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(new FrameReader.FrameData(100, 100, 0, 0),
                null, 1);
    }

    @Test
    public void construct_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of(),
                1
        );
    }

    @Test
    public void construct_Mipmap1MoreThanHalfWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(51, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap1LessThanHalfWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(49, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap2MoreThanQuarterWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(26, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap2LessThanQuarterWidth_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(24, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap1MoreThanHalfHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 101),
                        new MockCloseableImage(25, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap1LessThanHalfHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 99),
                        new MockCloseableImage(25, 50)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap2MoreThanQuarterHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 51)
                ),
                1
        );
    }

    @Test
    public void construct_Mipmap2LessThanQuarterHeight_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 49)
                ),
                1
        );
    }

    @Test
    public void construct_WidthNotDivisibleBy2_NoException() {
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50),
                        new MockCloseableImage(12, 25)
                ),
                1
        );
    }

    @Test
    public void construct_HeightNotDivisibleBy2_NoException() {
        new CloseableImageFrame(
                new FrameReader.FrameData(200, 100, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(200, 100),
                        new MockCloseableImage(100, 50),
                        new MockCloseableImage(50, 25),
                        new MockCloseableImage(25, 12)
                ),
                1
        );
    }

    @Test
    public void construct_ZeroLayers_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                0
        );
    }

    @Test
    public void construct_NegativeLayers_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                -1
        );
    }

    @Test
    public void construct_TooManyLayers_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                129
        );
    }

    @Test
    public void color_XBeyondWidth_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.color(100, 45);
    }

    @Test
    public void color_XNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.color(-10, 45);
    }

    @Test
    public void color_YBeyondHeight_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.color(45, 200);
    }

    @Test
    public void color_YNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.color(45, -10);
    }

    @Test
    public void color_InBounds_ColorRetrieved() {
        MockCloseableImage image = new MockCloseableImage(100, 200);
        image.setColor(45, 100, 1767640594);

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(image),
                1
        );

        assertEquals(1767640594, frame.color(45, 100));
    }

    @Test
    public void color_AlreadyClosed_IllegalStateException() {
        MockCloseableImage image = new MockCloseableImage(100, 200);
        image.setColor(45, 100, 1767640594);

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(image),
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.color(45, 100);
    }

    @Test
    public void width_WidthProvided_SameWidthReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        assertEquals(100, frame.width());
    }

    @Test
    public void width_AlreadyClosed_IllegalStateException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.width();
    }

    @Test
    public void height_HeightProvided_SameHeightReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        assertEquals(200, frame.height());
    }

    @Test
    public void height_AlreadyClosed_IllegalStateException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200)),
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.height();
    }

    @Test
    public void mipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        assertEquals(4, frame.mipmapLevel());
    }

    @Test
    public void mipmapLevel_AlreadyClosed_IllegalStateException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.mipmapLevel();
    }

    @Test
    public void upload_NegativeXPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(-1, 2, mipmaps.size() - 1);
    }

    @Test
    public void upload_NegativeYPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(1, -2, mipmaps.size() - 1);
    }

    @Test
    public void upload_NegativeBothPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(-1, -2, mipmaps.size() - 1);
    }

    @Test
    public void upload_NegativeMipmap_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(1, 2, -1);
    }

    @Test
    public void upload_MipmapTooLarge_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(1, 2, mipmaps.size());
    }

    @Test
    public void upload_ZeroPointOnlyBase_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(0, 0, 0);

        assertEquals((Long) Point.pack(0, 0), mipmaps.get(0).lastUploadPoint());
        assertNull(mipmaps.get(1).lastUploadPoint());
        assertNull(mipmaps.get(2).lastUploadPoint());
        assertNull(mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_ZeroPointSomeMipmaps_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(0, 0, mipmaps.size() - 3);

        assertEquals((Long) Point.pack(0, 0), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(2).lastUploadPoint());
        assertNull(mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_ZeroPointAllMipmaps_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(0, 0, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(0, 0), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointDividesToOrigin_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(6, 4, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(6, 4), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(3, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointDoesNotDivideToOrigin_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(55, 40, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(55, 40), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(27, 20), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(13, 10), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(6, 5), mipmaps.get(3).lastUploadPoint());
        assertEquals((Long) Point.pack(3, 2), mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_MipmapsDifferentSizes_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25),
                new MockCloseableImage(6, 12)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(55, 40, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(55, 40), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(27, 20), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(13, 10), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(6, 5), mipmaps.get(3).lastUploadPoint());
        assertEquals((Long) Point.pack(3, 2), mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointOnVerticalBorder_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 16),
                new MockCloseableImage(4, 8),
                new MockCloseableImage(2, 4),
                new MockCloseableImage(1, 2),
                new MockCloseableImage(0, 1)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(4, 16, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(4, 16), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 8), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 4), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 2), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointOnHorizontalBorder_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 16),
                new MockCloseableImage(4, 8),
                new MockCloseableImage(2, 4),
                new MockCloseableImage(1, 2),
                new MockCloseableImage(0, 1)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(8, 8, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(8, 8), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(4, 4), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 2), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointOnCorner_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 16),
                new MockCloseableImage(4, 8),
                new MockCloseableImage(2, 4),
                new MockCloseableImage(1, 2),
                new MockCloseableImage(0, 1)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(8, 16, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(8, 16), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(4, 8), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 4), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 2), mipmaps.get(3).lastUploadPoint());
        assertNull(null, mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_MipmapsEmptyWidth_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(10, 20),
                new MockCloseableImage(5, 10),
                new MockCloseableImage(2, 5),
                new MockCloseableImage(1, 2),
                new MockCloseableImage(0, 1)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(10, 20, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(5, 5, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_MipmapsEmptyHeight_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(20, 10),
                new MockCloseableImage(10, 5),
                new MockCloseableImage(5, 2),
                new MockCloseableImage(2, 1),
                new MockCloseableImage(1, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(20, 10, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(5, 5, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_MipmapsEmptyWidthAndHeight_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(10, 10),
                new MockCloseableImage(5, 5),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(10, 10, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(5, 5, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointOutsideMipmap_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(10, 10),
                new MockCloseableImage(5, 5),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(10, 10, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(500, 500, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(500, 500), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(250, 250), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(125, 125), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(62, 62), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_PointDividesOutsideMipmap_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        frame.uploadAt(5, 5, mipmaps.size() - 1);

        assertEquals((Long) Point.pack(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals((Long) Point.pack(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals((Long) Point.pack(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals((Long) Point.pack(0, 0), mipmaps.get(3).lastUploadPoint());
        assertNull(mipmaps.get(4).lastUploadPoint());
    }

    @Test
    public void upload_AlreadyClosed_IllegalStateException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25),
                new MockCloseableImage(6, 12)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps,
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.uploadAt(55, 40, mipmaps.size() - 1);
    }

    @Test
    public void lowerMipmapLevel_NegativeLevel_IllegalArtException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(-1);
    }

    @Test
    public void lowerMipmapLevel_ZeroLevel_MipmapsClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        int newLevel = 0;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_LessThanMaxLevel_MipmapsClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_MoreThanMaxLevel_IllegalArgException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        int newLevel = mipmaps.size();

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(newLevel);
    }

    @Test
    public void lowerMipmapLevel_MoreThanMaxLevel_NothingClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        int newLevel = mipmaps.size();

        try {
            frame.lowerMipmapLevel(newLevel);
        } catch (IllegalArgumentException ignored) {

        } finally {
            for (int level = 0; level < mipmaps.size(); level++) {
                assertFalse(mipmaps.get(level).isClosed());
            }
        }
    }

    @Test
    public void lowerMipmapLevel_AlreadyClosed_IllegalStateException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(4, 4),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(8, 8, 30, 40),
                mipmaps,
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);
    }

    @Test
    public void copyFrom_NullSource_NullPointerException() {
        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(NullPointerException.class);
        destination.copyFrom(null);
    }

    @Test
    public void copyFrom_SourceHasLowerMipLevel_IllegalArgException() {
        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100)
                ),
                1
        );

        expectedException.expect(IllegalArgumentException.class);
        destination.copyFrom(source);
    }

    @Test
    public void copyFrom_SourceHasHigherMipLevel_Copied() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100)
        );
        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );
        sources.get(0).setColor(45, 100, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(49, 15, 450605672);
        sources.get(1).setColor(30, 75, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                sources,
                1
        );

        destination.copyFrom(source);
        assertEquals(1177013896, destinations.get(0).color(45, 100));
        assertEquals(721898013, destinations.get(0).color(23, 10));
        assertEquals(450605672, destinations.get(1).color(49, 15));
        assertEquals(-557109892, destinations.get(1).color(30, 75));
    }

    @Test
    public void copyFrom_SourceHasSmallerWidthSmallerHeight_OnlyCommonAreaModified() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        // Outside smaller width, inside smaller height
        destinations.get(0).setColor(75, 57, 208915787);
        destinations.get(1).setColor(49, 15, -1839062270);
        destinations.get(2).setColor(12, 3, 796332458);

        // Inside smaller width, outside smaller height
        destinations.get(0).setColor(23, 100, 673006803);
        destinations.get(1).setColor(24, 75, -997731251);
        destinations.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(50, 100, 0, 0),
                sources,
                1
        );

        destination.copyFrom(source);
        assertEquals(1177013896, destinations.get(0).color(45, 99));
        assertEquals(721898013, destinations.get(0).color(23, 10));
        assertEquals(450605672, destinations.get(1).color(19, 15));
        assertEquals(-557109892, destinations.get(1).color(3, 49));
        assertEquals(-172022466, destinations.get(2).color(7, 3));
        assertEquals(-2092001461, destinations.get(2).color(1, 2));

        // Outside smaller width, inside smaller height
        assertEquals(208915787, destinations.get(0).color(75, 57));
        assertEquals(-997731251, destinations.get(1).color(24, 75));
        assertEquals(796332458, destinations.get(2).color(12, 3));

        // Inside smaller width, outside smaller height
        assertEquals(673006803, destinations.get(0).color(23, 100));
        assertEquals(-1839062270, destinations.get(1).color(49, 15));
        assertEquals(-1242096477, destinations.get(2).color(1, 25));

    }

    @Test
    public void copyFrom_SourceHasSmallerWidthLargerHeight_OnlyCommonAreaModified() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(100, 100),
                new MockCloseableImage(50, 50),
                new MockCloseableImage(25, 25)
        );

        // Outside smaller width, inside smaller height
        destinations.get(0).setColor(75, 57, 208915787);
        destinations.get(1).setColor(49, 15, -1839062270);
        destinations.get(2).setColor(12, 3, 796332458);

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(50, 200),
                new MockCloseableImage(25, 100),
                new MockCloseableImage(12, 50)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        // Inside smaller width, outside smaller height
        sources.get(0).setColor(23, 100, 673006803);
        sources.get(1).setColor(24, 75, -997731251);
        sources.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(50, 200, 0, 0),
                sources,
                1
        );

        destination.copyFrom(source);
        assertEquals(1177013896, destinations.get(0).color(45, 99));
        assertEquals(721898013, destinations.get(0).color(23, 10));
        assertEquals(450605672, destinations.get(1).color(19, 15));
        assertEquals(-557109892, destinations.get(1).color(3, 49));
        assertEquals(-172022466, destinations.get(2).color(7, 3));
        assertEquals(-2092001461, destinations.get(2).color(1, 2));

        assertEquals(208915787, destinations.get(0).color(75, 57));
        assertEquals(-1839062270, destinations.get(1).color(49, 15));
        assertEquals(796332458, destinations.get(2).color(12, 3));
    }

    @Test
    public void copyFrom_SourceHasLargerWidthSmallerHeight_OnlyCommonAreaModified() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(50, 200),
                new MockCloseableImage(25, 100),
                new MockCloseableImage(12, 50)
        );

        // Inside smaller width, outside smaller height
        destinations.get(0).setColor(23, 100, 673006803);
        destinations.get(1).setColor(24, 75, -997731251);
        destinations.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(50, 200, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(100, 100),
                new MockCloseableImage(50, 50),
                new MockCloseableImage(25, 25)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        // Outside smaller width, inside smaller height
        sources.get(0).setColor(75, 57, 208915787);
        sources.get(1).setColor(49, 15, -1839062270);
        sources.get(2).setColor(12, 3, 796332458);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                sources,
                1
        );

        destination.copyFrom(source);
        assertEquals(1177013896, destinations.get(0).color(45, 99));
        assertEquals(721898013, destinations.get(0).color(23, 10));
        assertEquals(450605672, destinations.get(1).color(19, 15));
        assertEquals(-557109892, destinations.get(1).color(3, 49));
        assertEquals(-172022466, destinations.get(2).color(7, 3));
        assertEquals(-2092001461, destinations.get(2).color(1, 2));

        // Inside smaller width, outside smaller height
        assertEquals(673006803, destinations.get(0).color(23, 100));
        assertEquals(-997731251, destinations.get(1).color(24, 75));
        assertEquals(-1242096477, destinations.get(2).color(1, 25));

    }

    @Test
    public void copyFrom_SourceHasLargerWidthLargerHeight_OnlyCommonAreaModified() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25)
        );

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(50, 100, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        // Outside smaller width, inside smaller height
        sources.get(0).setColor(75, 57, 208915787);
        sources.get(1).setColor(49, 15, -1839062270);
        sources.get(2).setColor(12, 3, 796332458);

        // Inside smaller width, outside smaller height
        sources.get(0).setColor(23, 100, 673006803);
        sources.get(1).setColor(24, 75, -997731251);
        sources.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                sources,
                1
        );

        destination.copyFrom(source);
        assertEquals(1177013896, destinations.get(0).color(45, 99));
        assertEquals(721898013, destinations.get(0).color(23, 10));
        assertEquals(450605672, destinations.get(1).color(19, 15));
        assertEquals(-557109892, destinations.get(1).color(3, 49));
        assertEquals(-172022466, destinations.get(2).color(7, 3));
        assertEquals(-2092001461, destinations.get(2).color(1, 2));
    }

    @Test
    public void copyFrom_AfterDestinationClose_IllegalStateException() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25)
        );

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(50, 100, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        // Outside smaller width, inside smaller height
        sources.get(0).setColor(75, 57, 208915787);
        sources.get(1).setColor(49, 15, -1839062270);
        sources.get(2).setColor(12, 3, 796332458);

        // Inside smaller width, outside smaller height
        sources.get(0).setColor(23, 100, 673006803);
        sources.get(1).setColor(24, 75, -997731251);
        sources.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                sources,
                1
        );

        destination.close();

        expectedException.expect(IllegalStateException.class);
        destination.copyFrom(source);
    }

    @Test
    public void copyFrom_AfterSourceClose_IllegalStateException() {
        ImmutableList<MockCloseableImage> destinations = ImmutableList.of(
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50),
                new MockCloseableImage(12, 25)
        );

        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(50, 100, 0, 0),
                destinations,
                1
        );

        ImmutableList<MockCloseableImage> sources = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );
        sources.get(0).setColor(45, 99, 1177013896);
        sources.get(0).setColor(23, 10, 721898013);
        sources.get(1).setColor(19, 15, 450605672);
        sources.get(1).setColor(3, 49, -557109892);
        sources.get(2).setColor(7, 3, -172022466);
        sources.get(2).setColor(1, 2, -2092001461);

        // Outside smaller width, inside smaller height
        sources.get(0).setColor(75, 57, 208915787);
        sources.get(1).setColor(49, 15, -1839062270);
        sources.get(2).setColor(12, 3, 796332458);

        // Inside smaller width, outside smaller height
        sources.get(0).setColor(23, 100, 673006803);
        sources.get(1).setColor(24, 75, -997731251);
        sources.get(2).setColor(1, 25, -1242096477);

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                sources,
                1
        );

        source.close();

        expectedException.expect(IllegalStateException.class);
        destination.copyFrom(source);
    }

    @Test
    public void applyTransform_NullTransform_NullPointerException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(NullPointerException.class);
        frame.applyTransform(null, Area.of(), 0);
    }

    @Test
    public void applyTransform_NullArea_NullPointerException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(NullPointerException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), null, 0);
    }

    @Test
    public void applyTransform_NegativeLayer_IllegalArgException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                5
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, 50)), -1);
    }

    @Test
    public void applyTransform_NonExistentLayer_IllegalArgException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                5
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, 50)), 5);
    }

    @Test
    public void applyTransform_LayerOverflowsByte_IllegalArgException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                128
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, 50)), 128);
    }

    @Test
    public void applyTransform_PointXTooLarge_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(100, 50)), 0);
    }

    @Test
    public void applyTransform_PointYTooLarge_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, 200)), 0);
    }

    @Test
    public void applyTransform_PointXNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(-1, 50)), 0);
    }

    @Test
    public void applyTransform_PointYNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, -1)), 0);
    }

    @Test
    public void applyTransform_DependencyPointTooLargeX_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> depFunction.color(100, 50), Area.of(Point.pack(50, 50)), 0);
    }

    @Test
    public void applyTransform_DependencyPointTooLargeY_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> depFunction.color(50, 200), Area.of(Point.pack(50, 50)), 0);
    }

    @Test
    public void applyTransform_DependencyPointNegativeX_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> depFunction.color(-1, 50), Area.of(Point.pack(50, 50)), 0);
    }

    @Test
    public void applyTransform_DependencyPointTooNegativeY_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        expectedException.expect(PixelOutOfBoundsException.class);
        frame.applyTransform((x, y, depFunction) -> depFunction.color(50, -1), Area.of(Point.pack(50, 50)), 0);
    }

    @Test
    public void applyTransform_EmptyMipmap_NonEmptyUpdated() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(2, 2, 0, 0),
                images,
                1
        );

        Long2IntMap pointToColor = new Long2IntOpenHashMap();
        pointToColor.put(Point.pack(0, 0), 1767640594);
        pointToColor.put(Point.pack(0, 1), 1177013896);
        pointToColor.put(Point.pack(1, 0), -557109892);
        pointToColor.put(Point.pack(1, 1), -172022466);

        frame.applyTransform(
                (x, y, depFunction) -> pointToColor.getOrDefault(Point.pack(x, y), 1013857456),
                Area.of(pointToColor.keySet()),
                0
        );

        for (long point : pointToColor.keySet()) {
            assertEquals(pointToColor.get(point), images.get(0).color(Point.x(point), Point.y(point)));
        }
        assertEquals(ColorBlender.blend(1767640594, 1177013896, -557109892, -172022466), images.get(1).color(0, 0));
    }

    @Test
    public void applyTransform_AllColorsInSquareChanged_OriginalUpdated() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                1
        );

        Long2IntMap pointToColor = new Long2IntOpenHashMap();
        pointToColor.put(Point.pack(48, 100), 1767640594);
        pointToColor.put(Point.pack(48, 101), 1177013896);
        pointToColor.put(Point.pack(48, 102), 721898013);
        pointToColor.put(Point.pack(48, 103), 450605672);
        pointToColor.put(Point.pack(49, 100), -557109892);
        pointToColor.put(Point.pack(49, 101), -172022466);
        pointToColor.put(Point.pack(49, 102), -2092001461);
        pointToColor.put(Point.pack(49, 103), 208915787);
        pointToColor.put(Point.pack(50, 100), 673006803);
        pointToColor.put(Point.pack(50, 101), -1839062270);
        pointToColor.put(Point.pack(50, 102), -997731251);
        pointToColor.put(Point.pack(50, 103), 796332458);
        pointToColor.put(Point.pack(51, 100), -1242096477);
        pointToColor.put(Point.pack(51, 101), -327745376);
        pointToColor.put(Point.pack(51, 102), -1450384761);
        pointToColor.put(Point.pack(51, 103), 1864744117);

        frame.applyTransform(
                (x, y, depFunction) -> pointToColor.getOrDefault(Point.pack(x, y), 1013857456),
                Area.of(pointToColor.keySet()),
                0
        );

        for (long point : pointToColor.keySet()) {
            assertEquals(pointToColor.get(point), images.get(0).color(Point.x(point), Point.y(point)));
        }
    }

    @Test
    public void applyTransform_AllColorsInSquareChanged_MipmapsBlended() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                1
        );

        Long2IntMap pointToColor = new Long2IntOpenHashMap();
        pointToColor.put(Point.pack(48, 100), 1767640594);
        pointToColor.put(Point.pack(48, 101), 1177013896);
        pointToColor.put(Point.pack(48, 102), 721898013);
        pointToColor.put(Point.pack(48, 103), 450605672);
        pointToColor.put(Point.pack(49, 100), -557109892);
        pointToColor.put(Point.pack(49, 101), -172022466);
        pointToColor.put(Point.pack(49, 102), -2092001461);
        pointToColor.put(Point.pack(49, 103), 208915787);
        pointToColor.put(Point.pack(50, 100), 673006803);
        pointToColor.put(Point.pack(50, 101), -1839062270);
        pointToColor.put(Point.pack(50, 102), -997731251);
        pointToColor.put(Point.pack(50, 103), 796332458);
        pointToColor.put(Point.pack(51, 100), -1242096477);
        pointToColor.put(Point.pack(51, 101), -327745376);
        pointToColor.put(Point.pack(51, 102), -1450384761);
        pointToColor.put(Point.pack(51, 103), 1864744117);

        frame.applyTransform(
                (x, y, depFunction) -> pointToColor.getOrDefault(Point.pack(x, y), 1013857456),
                Area.of(pointToColor.keySet()),
                0
        );

        int topLeftColorMip1 = ColorBlender.blend(1767640594, 1177013896, -557109892, -172022466);
        int topRightColorMip1 = ColorBlender.blend(673006803, -1839062270, -1242096477, -327745376);
        int bottomLeftColorMip1 = ColorBlender.blend(721898013, 450605672, -2092001461, 208915787);
        int bottomRightColorMip1 = ColorBlender.blend(-997731251, 796332458, -1450384761, 1864744117);

        assertEquals(topLeftColorMip1, images.get(1).color(24, 50));
        assertEquals(topRightColorMip1, images.get(1).color(25, 50));
        assertEquals(bottomLeftColorMip1, images.get(1).color(24, 51));
        assertEquals(bottomRightColorMip1, images.get(1).color(25, 51));

        int colorMip2 = ColorBlender.blend(
                topLeftColorMip1,
                topRightColorMip1,
                bottomLeftColorMip1,
                bottomRightColorMip1
        );
        assertEquals(colorMip2, images.get(2).color(12, 25));
    }

    @Test
    public void applyTransform_SomeColorsInSquareChanged_OriginalUpdated() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                1
        );

        Long2IntMap pointToColor = new Long2IntOpenHashMap();
        pointToColor.put(Point.pack(48, 100), 1767640594);
        images.get(0).setColor(48, 101, 1177013896);
        pointToColor.put(Point.pack(48, 102), 721898013);
        images.get(0).setColor(48, 103, 450605672);
        images.get(0).setColor(49, 100, -557109892);
        pointToColor.put(Point.pack(49, 101), -172022466);
        images.get(0).setColor(49, 102, -2092001461);
        pointToColor.put(Point.pack(49, 103), 208915787);
        images.get(0).setColor(50, 100, 673006803);
        pointToColor.put(Point.pack(50, 101), -1839062270);
        images.get(0).setColor(50, 102, -997731251);
        images.get(0).setColor(50, 103, 796332458);
        images.get(0).setColor(51, 100, -1242096477);
        pointToColor.put(Point.pack(51, 101), -327745376);
        images.get(0).setColor(51, 102, -1450384761);
        pointToColor.put(Point.pack(51, 103), 1864744117);

        frame.applyTransform(
                (x, y, depFunction) -> pointToColor.getOrDefault(Point.pack(x, y), 1013857456),
                Area.of(pointToColor.keySet()),
                0
        );

        assertEquals(1767640594, images.get(0).color(48, 100));
        assertEquals(1177013896, images.get(0).color(48, 101));
        assertEquals(721898013, images.get(0).color(48, 102));
        assertEquals(450605672, images.get(0).color(48, 103));
        assertEquals(-557109892, images.get(0).color(49, 100));
        assertEquals(-172022466, images.get(0).color(49, 101));
        assertEquals(-2092001461, images.get(0).color(49, 102));
        assertEquals(208915787, images.get(0).color(49, 103));
        assertEquals(673006803, images.get(0).color(50, 100));
        assertEquals(-1839062270, images.get(0).color(50, 101));
        assertEquals(-997731251, images.get(0).color(50, 102));
        assertEquals(796332458, images.get(0).color(50, 103));
        assertEquals(-1242096477, images.get(0).color(51, 100));
        assertEquals(-327745376, images.get(0).color(51, 101));
        assertEquals(-1450384761, images.get(0).color(51, 102));
        assertEquals(1864744117, images.get(0).color(51, 103));
    }

    @Test
    public void applyTransform_SomeColorsInSquareChanged_MipmapsBlended() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                1
        );

        Long2IntMap pointToColor = new Long2IntOpenHashMap();
        pointToColor.put(Point.pack(48, 100), 1767640594);
        images.get(0).setColor(48, 101, 1177013896);
        pointToColor.put(Point.pack(48, 102), 721898013);
        images.get(0).setColor(48, 103, 450605672);
        images.get(0).setColor(49, 100, -557109892);
        pointToColor.put(Point.pack(49, 101), -172022466);
        images.get(0).setColor(49, 102, -2092001461);
        pointToColor.put(Point.pack(49, 103), 208915787);
        images.get(0).setColor(50, 100, 673006803);
        pointToColor.put(Point.pack(50, 101), -1839062270);
        images.get(0).setColor(50, 102, -997731251);
        images.get(0).setColor(50, 103, 796332458);
        images.get(0).setColor(51, 100, -1242096477);
        pointToColor.put(Point.pack(51, 101), -327745376);
        images.get(0).setColor(51, 102, -1450384761);
        pointToColor.put(Point.pack(51, 103), 1864744117);

        frame.applyTransform(
                (x, y, depFunction) -> pointToColor.getOrDefault(Point.pack(x, y), 1013857456),
                Area.of(pointToColor.keySet()),
                0
        );

        int topLeftColorMip1 = ColorBlender.blend(1767640594, 1177013896, -557109892, -172022466);
        int topRightColorMip1 = ColorBlender.blend(673006803, -1839062270, -1242096477, -327745376);
        int bottomLeftColorMip1 = ColorBlender.blend(721898013, 450605672, -2092001461, 208915787);
        int bottomRightColorMip1 = ColorBlender.blend(-997731251, 796332458, -1450384761, 1864744117);

        assertEquals(topLeftColorMip1, images.get(1).color(24, 50));
        assertEquals(topRightColorMip1, images.get(1).color(25, 50));
        assertEquals(bottomLeftColorMip1, images.get(1).color(24, 51));
        assertEquals(bottomRightColorMip1, images.get(1).color(25, 51));

        int colorMip2 = ColorBlender.blend(
                topLeftColorMip1,
                topRightColorMip1,
                bottomLeftColorMip1,
                bottomRightColorMip1
        );
        assertEquals(colorMip2, images.get(2).color(12, 25));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToBottom_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToMiddle_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToTop_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                2
        );

        // The dependency is retrieved from the previous layer, which does not see 796332458 in the top layer
        assertEquals(450605672, images.get(0).color(48, 101));

    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToBottom_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToMiddle_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToTop_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToBottom_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToMiddle_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToTop_DependencyRetrievedFromCorrectLayer() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);
        images.get(0).setColor(48, 103, 450605672);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 103)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 103));

        frame.applyTransform(
                (x, y, depFunction) -> depFunction.color(48, 103),
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToBottom_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToMiddle_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToTopBeforeWritingToTop_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToBottom_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToMiddle_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToMiddleBeforeWritingToTop_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToBottom_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToMiddle_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                1
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_WriteToBottomBeforeWritingToTop_LowerDoesNotOverwriteUpper() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                3
        );

        images.get(0).setColor(48, 101, 1177013896);

        frame.applyTransform(
                (x, y, depFunction) -> 796332458,
                Area.of(Point.pack(48, 101)),
                0
        );

        assertEquals(796332458, images.get(0).color(48, 101));

        frame.applyTransform(
                (x, y, depFunction) -> 450605672,
                Area.of(Point.pack(48, 101)),
                2
        );

        assertEquals(450605672, images.get(0).color(48, 101));
    }

    @Test
    public void applyTransform_AfterClose_IllegalStateException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.applyTransform((x, y, depFunction) -> Color.pack(100, 100, 100, 100), Area.of(Point.pack(50, 100)), 0);
    }

    @Test
    public void close_CloseTwice_NoException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                ),
                1
        );

        frame.close();
        frame.close();
    }

    @Test
    public void close_ImagesOpen_ImagesClosed() {
        ImmutableList<MockCloseableImage> images = ImmutableList.of(
                new MockCloseableImage(100, 200),
                new MockCloseableImage(50, 100),
                new MockCloseableImage(25, 50)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                images,
                1
        );

        assertFalse(images.get(0).isClosed());
        assertFalse(images.get(1).isClosed());
        assertFalse(images.get(2).isClosed());

        frame.close();

        assertTrue(images.get(0).isClosed());
        assertTrue(images.get(1).isClosed());
        assertTrue(images.get(2).isClosed());
    }

}