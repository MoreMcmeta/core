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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

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
        new CloseableImageFrame(null, mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1));
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(new FrameReader.FrameData(100, 100, 0, 0),
                null, new CloseableImageFrame.SharedMipmapLevel(0));
    }

    @Test
    public void getMipmapLevel_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of(), new CloseableImageFrame.SharedMipmapLevel(0)
        );
    }

    @Test
    public void construct_SharedLevelNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                ImmutableList.of(new MockCloseableImage()), null
        );
    }

    @Test
    public void construct_SharedLevelLessThanMipmaps_MipmapLevelLoweredImmediately() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(new MockCloseableImage(), new MockCloseableImage(),
                new MockCloseableImage());
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(1)
        );

        assertEquals(1, frame.getMipmapLevel());
        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(2);
    }

    @Test
    public void construct_SharedLevelLessThanMipmaps_ExtraMipmapsClosedImmediately() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(new MockCloseableImage(), new MockCloseableImage(),
                new MockCloseableImage());
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(1)
        );

        assertTrue(mipmaps.get(2).isClosed());
    }

    @Test
    public void construct_SharedLevelMoreThanMipmaps_IllegalArgException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(new MockCloseableImage(), new MockCloseableImage(),
                new MockCloseableImage());

        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(3)
        );
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage()), new CloseableImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage()), new CloseableImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getXOffset_XOffsetProvided_SameXOffsetReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of(new MockCloseableImage()), new CloseableImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(30, frame.getXOffset());
    }

    @Test
    public void getYOffset_YOffsetProvided_SameYOffsetReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of(new MockCloseableImage()), new CloseableImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(40, frame.getYOffset());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        assertEquals(4, frame.getMipmapLevel());
    }

    @Test
    public void getMipmap_MipmapAtArrayLength_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(5);
    }

    @Test
    public void getMipmap_MipmapBeyondArrayLength_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(6);
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
    }

    @Test
    public void upload_NegativeXPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, 2));
    }

    @Test
    public void upload_NegativeYPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(1, -2));
    }

    @Test
    public void upload_NegativeBothPoint_IllegalArgException() {
        ImmutableList<CloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, -2));
    }

    @Test
    public void upload_ZeroPoint_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(0, 0));

        assertEquals(new Point(0, 0), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointDividesToOrigin_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(6, 4));

        assertEquals(new Point(6, 4), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointDoesNotDivideToOrigin_AllUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage(),
                new MockCloseableImage()
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(55, 40));

        assertEquals(new Point(55, 40), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(27, 20), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(13, 10), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(6, 5), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(4).getLastUploadPoint());
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
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(55, 40));

        assertEquals(new Point(55, 40), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(27, 20), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(13, 10), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(6, 5), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(4, 16));

        assertEquals(new Point(4, 16), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 8), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 4), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 2), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(8, 8));

        assertEquals(new Point(8, 8), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(4, 4), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(8, 16));

        assertEquals(new Point(8, 16), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(4, 8), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(2, 4), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(1, 2), mipmaps.get(3).getLastUploadPoint());
        assertNull(null, mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(500, 500));

        assertEquals(new Point(500, 500), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(250, 250), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(125, 125), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(62, 62), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointDividesOutsideMipmap_EmptyNotUploaded() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void lowerMipmapLevel_NegativeLevel_IllegalArtException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(-1);
    }

    @Test
    public void lowerMipmapLevel_ZeroLevel_MipmapsClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 0;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_ZeroLevel_ClosedMipmapsNoLongerProvided() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 0;
        frame.lowerMipmapLevel(newLevel);

        int mipmapsNotClosed = mipmaps.size() - 1;

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            try {
                frame.getImage(level);
            } catch (IllegalArgumentException err) {
                mipmapsNotClosed--;
            }
        }

        assertEquals(0, mipmapsNotClosed);
    }

    @Test
    public void lowerMipmapLevel_LessThanMaxLevel_MipmapsClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_LessThanMaxLevel_ClosedMipmapsNoLongerProvided() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);

        int mipmapsNotClosed = mipmaps.size() - 1;

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            try {
                frame.getImage(level);
            } catch (IllegalArgumentException err) {
                mipmapsNotClosed--;
            }
        }

        assertEquals(newLevel, mipmapsNotClosed);
    }

    @Test
    public void lowerMipmapLevel_MaxLevel_NothingClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size() - 1;
        frame.lowerMipmapLevel(newLevel);

        for (int level = 0; level < mipmaps.size(); level++) {
            assertFalse(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_MaxLevel_MipmapsStillProvided() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size() - 1;
        frame.lowerMipmapLevel(newLevel);

        int mipmapsNotClosed = mipmaps.size() - 1;

        for (int level = 0; level < mipmaps.size(); level++) {
            try {
                frame.getImage(level);
            } catch (IllegalArgumentException err) {
                mipmapsNotClosed--;
            }
        }

        assertEquals(mipmaps.size() - 1, mipmapsNotClosed);
    }

    @Test
    public void lowerMipmapLevel_MoreThanMaxLevel_IllegalArgException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size();

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(newLevel);
    }

    @Test
    public void lowerMipmapLevel_MoreThanMaxLevel_NothingClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
    public void lowerMipmapLevel_MoreThanMaxLevel_MipmapsStillProvided() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size();

        try {
            frame.lowerMipmapLevel(newLevel);
        } catch (IllegalArgumentException ignored) {

        } finally {

            // No exceptions should be thrown here
            for (int level = 0; level < mipmaps.size(); level++) {
                frame.getImage(level);
            }

        }
    }

    @Test
    public void lowerMipmapLevel_LoweredByAnotherFrame_MipmapsClosed() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        ImmutableList<MockCloseableImage> mipmaps2 = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame frame2 = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps2, sharedLevel
        );

        int newLevel = 2;
        frame2.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_LoweredByAnotherFrame_ClosedMipmapsNoLongerProvided() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        ImmutableList<MockCloseableImage> mipmaps2 = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame frame2 = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps2, sharedLevel
        );

        int newLevel = 2;
        frame2.lowerMipmapLevel(newLevel);

        int mipmapsNotClosed = mipmaps.size() - 1;

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            try {
                frame.getImage(level);
            } catch (IllegalArgumentException err) {
                mipmapsNotClosed--;
            }
        }

        assertEquals(newLevel, mipmapsNotClosed);
    }

    @Test
    public void constructSharedLevel_NegativeLevel_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame.SharedMipmapLevel(-1);
    }

    @Test
    public void constructSharedLevel_IntMinLevel_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame.SharedMipmapLevel(Integer.MIN_VALUE);
    }

    @Test
    public void constructSharedLevel_ZeroLevel_NoException() {
        new CloseableImageFrame.SharedMipmapLevel(0);
    }

    @Test
    public void constructSharedLevel_PositiveLevel_NoException() {
        new CloseableImageFrame.SharedMipmapLevel(1);
    }

    @Test
    public void constructSharedLevel_IntMaxLevel_NoException() {
        new CloseableImageFrame.SharedMipmapLevel(Integer.MAX_VALUE);
    }

    @Test
    public void lowerSharedLevel_SameAsCurrent_LevelNotLowered() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 1);
        assertEquals(mipmaps.size() - 1, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_LargerThanCurrent_IllegalArgException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        expectedException.expect(IllegalArgumentException.class);
        sharedLevel.lowerMipmapLevel(mipmaps.size());
    }

    @Test
    public void lowerSharedLevel_LessThanCurrent_LevelLowered() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
        assertEquals(mipmaps.size() - 2, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_Negative_IllegalArgException() {
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(4);
        expectedException.expect(IllegalArgumentException.class);
        sharedLevel.lowerMipmapLevel(-1);
    }

    @Test
    public void lowerSharedLevel_SubscriberAddedAfterFirstLowering_LevelLowered() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);

        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );
        sharedLevel.lowerMipmapLevel(mipmaps.size() - 3);
        assertEquals(mipmaps.size() - 3, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_NoSubscribers_NoException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
    }

    @Test
    public void addSubscriber_NullFrame_NullPointerException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        expectedException.expect(NullPointerException.class);
        sharedLevel.addSubscriber(null);
    }

    @Test
    public void addSubscriber_DuplicateFrame_NoException() {
        ImmutableList<MockCloseableImage> mipmaps = ImmutableList.of(
                new MockCloseableImage(8, 8),
                new MockCloseableImage(2, 2),
                new MockCloseableImage(1, 1),
                new MockCloseableImage(0, 0),
                new MockCloseableImage(0, 0)
        );
        CloseableImageFrame.SharedMipmapLevel sharedLevel = new CloseableImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                mipmaps, sharedLevel
        );

        // Frame already subscribes to the shared level
        sharedLevel.addSubscriber(frame);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
        assertEquals(mipmaps.size() - 2, frame.getMipmapLevel());
    }

}