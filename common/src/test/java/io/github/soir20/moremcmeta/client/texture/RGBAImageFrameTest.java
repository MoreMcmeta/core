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
import io.github.soir20.moremcmeta.api.Image;
import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link RGBAImageFrame}.
 * @author soir20
 */
public class RGBAImageFrameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_FrameDataNull_NullPointerException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(new MockRGBAImage());

        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame(null, mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1));
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame(new FrameReader.FrameData(100, 100, 0, 0, 10),
                null, new RGBAImageFrame.SharedMipmapLevel(0));
    }

    @Test
    public void getMipmapLevel_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of(), new RGBAImageFrame.SharedMipmapLevel(0)
        );
    }

    @Test
    public void construct_SharedLevelNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage()), null
        );
    }

    @Test
    public void construct_SharedLevelLessThanMipmaps_MipmapLevelLoweredImmediately() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(new MockRGBAImage(), new MockRGBAImage(),
                new MockRGBAImage());
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(1)
        );

        assertEquals(1, frame.getMipmapLevel());
        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(2);
    }

    @Test
    public void construct_SharedLevelLessThanMipmaps_ExtraMipmapsClosedImmediately() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(new MockRGBAImage(), new MockRGBAImage(),
                new MockRGBAImage());
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(1)
        );

        assertTrue(mipmaps.get(2).isClosed());
    }

    @Test
    public void construct_SharedLevelMoreThanMipmaps_IllegalArgException() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(new MockRGBAImage(), new MockRGBAImage(),
                new MockRGBAImage());

        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(3)
        );
    }

    @Test
    public void getFrameTime_NotEmptyTime_SameTimeReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(10, frame.getFrameTime());
    }

    @Test
    public void getFrameTime_EmptyTime_SameTimeReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, FrameReader.FrameData.EMPTY_TIME),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getXOffset_XOffsetProvided_SameXOffsetReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(30, frame.getXOffset());
    }

    @Test
    public void getYOffset_YOffsetProvided_SameYOffsetReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of(new MockRGBAImage()), new RGBAImageFrame.SharedMipmapLevel(0)
        );

        assertEquals(40, frame.getYOffset());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        assertEquals(4, frame.getMipmapLevel());
    }

    @Test
    public void getMipmap_MipmapAtArrayLength_IllegalArgException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(5);
    }

    @Test
    public void getMipmap_MipmapBeyondArrayLength_IllegalArgException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(6);
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
    }

    @Test
    public void upload_NegativeXPoint_IllegalArgException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, 2));
    }

    @Test
    public void upload_NegativeYPoint_IllegalArgException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(1, -2));
    }

    @Test
    public void upload_NegativeBothPoint_IllegalArgException() {
        ImmutableList<RGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, -2));
    }

    @Test
    public void upload_ZeroPoint_AllUploaded() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(100, 200),
                new MockRGBAImage(50, 100),
                new MockRGBAImage(25, 50),
                new MockRGBAImage(12, 25),
                new MockRGBAImage(6, 12)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 20),
                new MockRGBAImage(5, 10),
                new MockRGBAImage(2, 5),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(20, 10),
                new MockRGBAImage(10, 5),
                new MockRGBAImage(5, 2),
                new MockRGBAImage(2, 1),
                new MockRGBAImage(1, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(-1);
    }

    @Test
    public void lowerMipmapLevel_ZeroLevel_MipmapsClosed() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 0;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_ZeroLevel_ClosedMipmapsNoLongerProvided() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = 2;
        frame.lowerMipmapLevel(newLevel);

        for (int level = newLevel + 1; level < mipmaps.size(); level++) {
            assertTrue(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_LessThanMaxLevel_ClosedMipmapsNoLongerProvided() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size() - 1;
        frame.lowerMipmapLevel(newLevel);

        for (int level = 0; level < mipmaps.size(); level++) {
            assertFalse(mipmaps.get(level).isClosed());
        }
    }

    @Test
    public void lowerMipmapLevel_MaxLevel_MipmapsStillProvided() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
        );

        int newLevel = mipmaps.size();

        expectedException.expect(IllegalArgumentException.class);
        frame.lowerMipmapLevel(newLevel);
    }

    @Test
    public void lowerMipmapLevel_MoreThanMaxLevel_NothingClosed() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1)
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        ImmutableList<MockRGBAImage> mipmaps2 = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame frame2 = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        ImmutableList<MockRGBAImage> mipmaps2 = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame frame2 = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
    public void constructInterpolator_NullMipmaps_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame.Interpolator(null);
    }

    @Test
    public void constructInterpolator_EmptyMipmaps_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame.Interpolator(ImmutableList.of());
    }

    @Test
    public void interpolate_NullStartFrame_NullPointerException() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(NullPointerException.class);
        interpolator.interpolate(10, 3, null, new MockRGBAImageFrame(10, 10));
    }

    @Test
    public void interpolate_NullEndFrame_NullPointerException() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(NullPointerException.class);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10), null);
    }

    @Test
    public void interpolate_StepLessThanOne_IllegalArgException() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 0, new MockRGBAImageFrame(10, 10),
                new MockRGBAImageFrame(10, 10));
    }

    @Test
    public void interpolate_StepEqualsSteps_IllegalArgException() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 10, new MockRGBAImageFrame(10, 10),
                new MockRGBAImageFrame(10, 10));
    }

    @Test
    public void interpolate_StepGreaterThanSteps_IllegalArgException() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 11, new MockRGBAImageFrame(10, 10),
                new MockRGBAImageFrame(10, 10));
    }

    @Test
    public void interpolate_StartFrameFewerMipmaps_ResultMipmapLowered() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        RGBAImageFrame result = interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 2),
                new MockRGBAImageFrame(10, 10, 3));
        assertEquals(2, result.getMipmapLevel());
    }

    @Test
    public void interpolate_StartFrameFewerMipmaps_ResultMipmapsClosed() {
        ImmutableList<MockRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 2),
                new MockRGBAImageFrame(10, 10, 3));
        assertTrue(frames.get(3).isClosed());
    }

    @Test
    public void interpolate_StartFrameFewerMipmaps_SubsequentResultsLowered() {
        ImmutableList<MockRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 2),
                new MockRGBAImageFrame(10, 10, 3));
        RGBAImageFrame result2 = interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 3));
        assertEquals(2, result2.getMipmapLevel());
    }

    @Test
    public void interpolate_EndFrameFewerMipmaps_ResultMipmapLowered() {
        ImmutableList<RGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        RGBAImageFrame result = interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 2));
        assertEquals(2, result.getMipmapLevel());
    }

    @Test
    public void interpolate_EndFrameFewerMipmaps_ResultMipmapsClosed() {
        ImmutableList<MockRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 2));
        assertTrue(frames.get(3).isClosed());
    }

    @Test
    public void interpolate_EndFrameFewerMipmaps_SubsequentResultsLowered() {
        ImmutableList<MockRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 2));
        RGBAImageFrame result2 = interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 3));
        assertEquals(2, result2.getMipmapLevel());
    }

    @Test
    public void interpolate_SameMipmaps_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 3);

        for (int level = 0; level <= 3; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(6 >> level, 7 >> level);
            frameBuilder.add(new MockRGBAImage(new int[10 >> level][10 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(138, 186, 178, 85));
        }

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 3; level++) {
            int color = frame.getImage(level).getPixel(6 >> level, 7 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void interpolate_StartFrameMoreMipmaps_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 2);

        for (int level = 0; level <= 2; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(6 >> level, 7 >> level);
            frameBuilder.add(new MockRGBAImage(new int[10 >> level][10 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(138, 186, 178, 85));
        }

        startFrame.getImage(3).setPixel(6 >> 4, 7 >> 4, toBinary(251, 113, 66, 76));

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 2; level++) {
            int color = frame.getImage(level).getPixel(6 >> level, 7 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void interpolate_EndFrameMoreMipmaps_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 3);

        for (int level = 0; level <= 2; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(6 >> level, 7 >> level);
            frameBuilder.add(new MockRGBAImage(new int[10 >> level][10 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(6 >> level, 7 >> level, toBinary(138, 186, 178, 85));
        }

        endFrame.getImage(3).setPixel(6 >> 4, 7 >> 4, toBinary(251, 113, 66, 76));

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 2; level++) {
            int color = frame.getImage(level).getPixel(6 >> level, 7 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void interpolate_EmptyMipmap_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(5, 5, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 5, 3);

        for (int level = 0; level <= 2; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(3 >> level, 2 >> level);
            frameBuilder.add(new MockRGBAImage(new int[5 >> level][5 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(138, 186, 178, 85));
        }

        frameBuilder.add(new MockRGBAImage(0, 0));

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 2; level++) {
            int color = frame.getImage(level).getPixel(3 >> level, 2 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void interpolate_StartOrEndLarger_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 5, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 10, 2);

        for (int level = 0; level <= 2; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(3 >> level, 2 >> level);
            frameBuilder.add(new MockRGBAImage(new int[5 >> level][5 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(138, 186, 178, 85));
        }

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 2; level++) {
            int color = frame.getImage(level).getPixel(3 >> level, 2 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void interpolate_StartOrEndSmaller_CorrectInterpolation() {
        ImmutableList.Builder<RGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 5, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 10, 2);

        for (int level = 0; level <= 2; level++) {
            Image.VisibleArea.Builder areaBuilder = new Image.VisibleArea.Builder();
            areaBuilder.addPixel(3 >> level, 2 >> level);
            frameBuilder.add(new MockRGBAImage(new int[10 >> level][10 >> level], areaBuilder.build()));

            startFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(251, 113, 66, 76));
            endFrame.getImage(level).setPixel(3 >> level, 2 >> level, toBinary(138, 186, 178, 85));
        }

        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frameBuilder.build());
        RGBAImageFrame frame = interpolator.interpolate(10, 3, startFrame, endFrame);
        for (int level = 0; level <= 2; level++) {
            int color = frame.getImage(level).getPixel(3 >> level, 2 >> level);
            assertEquals(toBinary(217, 134, 99, 76), color);
        }
    }

    @Test
    public void constructSharedLevel_NegativeLevel_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame.SharedMipmapLevel(-1);
    }

    @Test
    public void constructSharedLevel_IntMinLevel_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame.SharedMipmapLevel(Integer.MIN_VALUE);
    }

    @Test
    public void constructSharedLevel_ZeroLevel_NoException() {
        new RGBAImageFrame.SharedMipmapLevel(0);
    }

    @Test
    public void constructSharedLevel_PositiveLevel_NoException() {
        new RGBAImageFrame.SharedMipmapLevel(1);
    }

    @Test
    public void constructSharedLevel_IntMaxLevel_NoException() {
        new RGBAImageFrame.SharedMipmapLevel(Integer.MAX_VALUE);
    }

    @Test
    public void lowerSharedLevel_SameAsCurrent_LevelNotLowered() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 1);
        assertEquals(mipmaps.size() - 1, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_LargerThanCurrent_IllegalArgException() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        expectedException.expect(IllegalArgumentException.class);
        sharedLevel.lowerMipmapLevel(mipmaps.size());
    }

    @Test
    public void lowerSharedLevel_LessThanCurrent_LevelLowered() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
        assertEquals(mipmaps.size() - 2, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_Negative_IllegalArgException() {
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(4);
        expectedException.expect(IllegalArgumentException.class);
        sharedLevel.lowerMipmapLevel(-1);
    }

    @Test
    public void lowerSharedLevel_SubscriberAddedAfterFirstLowering_LevelLowered() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );
        sharedLevel.lowerMipmapLevel(mipmaps.size() - 3);
        assertEquals(mipmaps.size() - 3, frame.getMipmapLevel());
    }

    @Test
    public void lowerSharedLevel_NoSubscribers_NoException() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
    }

    @Test
    public void addSubscriber_NullFrame_NullPointerException() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);

        expectedException.expect(NullPointerException.class);
        sharedLevel.addSubscriber(null);
    }

    @Test
    public void addSubscriber_DuplicateFrame_NoException() {
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );
        RGBAImageFrame.SharedMipmapLevel sharedLevel = new RGBAImageFrame.SharedMipmapLevel(mipmaps.size() - 1);
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps, sharedLevel
        );

        // Frame already subscribes to the shared level
        sharedLevel.addSubscriber(frame);

        sharedLevel.lowerMipmapLevel(mipmaps.size() - 2);
        assertEquals(mipmaps.size() - 2, frame.getMipmapLevel());
    }

    private int toBinary(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

}