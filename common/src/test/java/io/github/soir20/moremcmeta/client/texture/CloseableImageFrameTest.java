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
        new CloseableImageFrame(null, mipmaps);
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new CloseableImageFrame(new FrameReader.FrameData(100, 100, 0, 0),
                null);
    }

    @Test
    public void getMipmapLevel_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40),
                ImmutableList.of()
        );
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        CloseableImageFrame frame = new CloseableImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0),
                ImmutableList.of(new MockCloseableImage(100, 200))
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
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

        assertEquals(4, frame.getMipmapLevel());
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

        assertEquals(new Point(0, 0), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).getLastUploadPoint());
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

        assertEquals(new Point(6, 4), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(3, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(4).getLastUploadPoint());
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
                mipmaps
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
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(8, 16, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(10, 20, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(20, 10, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(10, 10, 30, 40),
                mipmaps
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
                new FrameReader.FrameData(10, 10, 30, 40),
                mipmaps
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

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertEquals(new Point(0, 0), mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
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

}