/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
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
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(new MockRGBAImage());

        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame(null, mipmaps);
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame(new FrameReader.FrameData(100, 100, 0, 0, 10),
                null);
    }

    @Test
    public void getMipmapLevel_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of()
        );
    }

    @Test
    public void getFrameTime_NotEmptyTime_SameTimeReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(10, frame.getFrameTime());
    }

    @Test
    public void getFrameTime_EmptyTime_SameTimeReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, FrameReader.FrameData.EMPTY_TIME),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getXOffset_XOffsetProvided_SameXOffsetReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(30, frame.getXOffset());
    }

    @Test
    public void getYOffset_YOffsetProvided_SameYOffsetReturned() {
        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                ImmutableList.of(new MockRGBAImage())
        );

        assertEquals(40, frame.getYOffset());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        assertEquals(4, frame.getMipmapLevel());
    }

    @Test
    public void getMipmap_MipmapAtArrayLength_IllegalArgException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(5);
    }

    @Test
    public void getMipmap_MipmapBeyondArrayLength_IllegalArgException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(6);
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
    }

    @Test
    public void upload_NegativeXPoint_IllegalArgException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, 2));
    }

    @Test
    public void upload_NegativeYPoint_IllegalArgException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(1, -2));
    }

    @Test
    public void upload_NegativeBothPoint_IllegalArgException() {
        ImmutableList<IRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage(),
                new MockRGBAImage()
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(100, 200),
                new MockRGBAImage(50, 100),
                new MockRGBAImage(25, 50),
                new MockRGBAImage(12, 25),
                new MockRGBAImage(6, 12)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 16),
                new MockRGBAImage(4, 8),
                new MockRGBAImage(2, 4),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 20),
                new MockRGBAImage(5, 10),
                new MockRGBAImage(2, 5),
                new MockRGBAImage(1, 2),
                new MockRGBAImage(0, 1)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(20, 10),
                new MockRGBAImage(10, 5),
                new MockRGBAImage(5, 2),
                new MockRGBAImage(2, 1),
                new MockRGBAImage(1, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
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
        ImmutableList<MockRGBAImage> mipmaps = ImmutableList.of(
                new MockRGBAImage(8, 8),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1),
                new MockRGBAImage(0, 0),
                new MockRGBAImage(0, 0)
        );

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertEquals(new Point(2, 2), mipmaps.get(1).getLastUploadPoint());
        assertEquals(new Point(1, 1), mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void constructInterpolator_NullMipmaps_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new RGBAImageFrame.Interpolator(null);
    }

    @Test
    public void interpolate_NullStartFrame_NullPointerException() {
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
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
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
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
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
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
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
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
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
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
    public void interpolate_StartFrameFewerMipmaps_IllegalArgException() {
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 2),
                new MockRGBAImageFrame(10, 10, 3));
    }

    @Test
    public void interpolate_EndFrameFewerMipmaps_IllegalArgException() {
        ImmutableList<IRGBAImage> frames = ImmutableList.of(
                new MockRGBAImage(10, 10),
                new MockRGBAImage(5, 5),
                new MockRGBAImage(2, 2),
                new MockRGBAImage(1, 1)
        );
        RGBAImageFrame.Interpolator interpolator = new RGBAImageFrame.Interpolator(frames);
        expectedException.expect(IllegalArgumentException.class);
        interpolator.interpolate(10, 3, new MockRGBAImageFrame(10, 10, 3),
                new MockRGBAImageFrame(10, 10, 2));
    }

    @Test
    public void interpolate_SameMipmaps_CorrectInterpolation() {
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 3);

        for (int level = 0; level <= 3; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 2);

        for (int level = 0; level <= 2; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 10, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(10, 10, 3);

        for (int level = 0; level <= 2; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(5, 5, 3);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 5, 3);

        for (int level = 0; level <= 2; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 5, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 10, 2);

        for (int level = 0; level <= 2; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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
        ImmutableList.Builder<IRGBAImage> frameBuilder = new ImmutableList.Builder<>();

        MockRGBAImageFrame startFrame = new MockRGBAImageFrame(10, 5, 2);
        MockRGBAImageFrame endFrame = new MockRGBAImageFrame(5, 10, 2);

        for (int level = 0; level <= 2; level++) {
            IRGBAImage.VisibleArea.Builder areaBuilder = new IRGBAImage.VisibleArea.Builder();
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

    private int toBinary(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

}