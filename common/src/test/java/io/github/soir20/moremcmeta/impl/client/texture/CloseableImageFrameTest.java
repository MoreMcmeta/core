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
public class CloseableImageFrameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_FrameDataNull_NullPointerException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(new MockCloseableImage());

        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(null, mipmaps);
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(new FrameReader.FrameData(100, 100, 0, 0),
                null);
    }

    @Test
    public void construct_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of()
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
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
                )
        );
    }

    @Test
    public void color_XBeyondWidth_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        frame.color(100, 45);
    }

    @Test
    public void color_XNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        frame.color(-10, 45);
    }

    @Test
    public void color_YBeyondHeight_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        frame.color(45, 200);
    }

    @Test
    public void color_YNegative_ExceptionFromImage() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        expectedException.expect(MockCloseableImage.MockPixelOutOfBoundsException.class);
        frame.color(45, -10);
    }

    @Test
    public void color_InBounds_ColorRetrieved() {
        MockCloseableImage image = new MockCloseableImage(100, 200);
        image.setColor(45, 100, 1767640594);

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(image)
        );

        assertEquals(1767640594, frame.color(45, 100));
    }

    @Test
    public void color_AlreadyClosed_IllegalStateException() {
        MockCloseableImage image = new MockCloseableImage(100, 200);
        image.setColor(45, 100, 1767640594);

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(image)
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.color(45, 100);
    }

    @Test
    public void width_WidthProvided_SameWidthReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        assertEquals(100, frame.width());
    }

    @Test
    public void width_AlreadyClosed_IllegalStateException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.width();
    }

    @Test
    public void height_HeightProvided_SameHeightReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        assertEquals(200, frame.height());
    }

    @Test
    public void height_AlreadyClosed_IllegalStateException() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
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
                mipmaps
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
                mipmaps
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.mipmapLevel();
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
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
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, 2));
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
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(1, -2));
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
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, -2));
    }

    @Test
    public void upload_ZeroPoint_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(128, 256),
                new MockCloseableImage(64, 128),
                new MockCloseableImage(32, 64),
                new MockCloseableImage(16, 32),
                new MockCloseableImage(8, 16)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(128, 256, 30, 40),
                mipmaps
        );

        frame.uploadAt(new Point(0, 0));

        assertEquals(new Point(0, 0), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(6, 4));

        assertEquals(new Point(6, 4), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(55, 40));

        assertEquals(new Point(55, 40), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(27, 20), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(13, 10), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(6, 5), mipmaps.get(3).lastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(4).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(55, 40));

        assertEquals(new Point(55, 40), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(27, 20), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(13, 10), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(6, 5), mipmaps.get(3).lastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(4).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(4, 16));

        assertEquals(new Point(4, 16), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(2, 8), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 4), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 2), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(8, 8));

        assertEquals(new Point(8, 8), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(4, 4), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(8, 16));

        assertEquals(new Point(8, 16), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(4, 8), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(2, 4), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(1, 2), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(500, 500));

        assertEquals(new Point(500, 500), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(250, 250), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(125, 125), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(62, 62), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).lastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).lastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).lastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).lastUploadPoint());
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
                mipmaps
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        frame.uploadAt(new Point(55, 40));
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
                mipmaps
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
                mipmaps
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
                mipmaps
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
                mipmaps
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
                mipmaps
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
                mipmaps
        );

        frame.close();

        expectedException.expect(IllegalStateException.class);
        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);
    }

    @Test
    public void copyFrom_NullSource_NullPointerException() {
        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                )
        );

        expectedException.expect(NullPointerException.class);
        source.copyFrom(null);
    }

    @Test
    public void copyFrom_SourceHasLowerMipLevel_IllegalArgException() {
        CloseableImageFrame destination = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100),
                        new MockCloseableImage(25, 50)
                )
        );

        CloseableImageFrame source = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(
                        new MockCloseableImage(100, 200),
                        new MockCloseableImage(50, 100)
                )
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
                destinations
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
                sources
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
                destinations
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
                sources
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
                destinations
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
                sources
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
                destinations
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
                sources
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
                destinations
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
                sources
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
                destinations
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
                sources
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
                destinations
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
                sources
        );

        source.close();

        expectedException.expect(IllegalStateException.class);
        destination.copyFrom(source);
    }

}