package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

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
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

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
        List<IRGBAImage> mipmaps = new ArrayList<>();

        expectedException.expect(IllegalArgumentException.class);
        new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );
    }

    @Test
    public void getFrameTime_NotEmptyTime_SameTimeReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                mipmaps
        );

        assertEquals(10, frame.getFrameTime());
    }

    @Test
    public void getFrameTime_EmptyTime_SameTimeReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, FrameReader.FrameData.EMPTY_TIME),
                mipmaps
        );

        assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                mipmaps
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                mipmaps
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getXOffset_XOffsetProvided_SameXOffsetReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        assertEquals(30, frame.getXOffset());
    }

    @Test
    public void getYOffset_YOffsetProvided_SameYOffsetReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        assertEquals(40, frame.getYOffset());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        assertEquals(4, frame.getMipmapLevel());
    }

    @Test
    public void getMipmap_MipmapAtArrayLength_IllegalArgException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(5);
    }

    @Test
    public void getMipmap_MipmapBeyondArrayLength_IllegalArgException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(6);
    }

    @Test
    public void getMipmap_MipmapNull_Null() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(null);
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        assertNull(frame.getImage(2));
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
    }

    @Test
    public void upload_NegativeXPoint_IllegalArgException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, 2));
    }

    @Test
    public void upload_NegativeYPoint_IllegalArgException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(1, -2));
    }

    @Test
    public void upload_NegativeBothPoint_IllegalArgException() {
        List<IRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.uploadAt(new Point(-1, -2));
    }

    @Test
    public void upload_ZeroPoint_AllUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

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
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

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
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());
        mipmaps.add(new MockRGBAImage());

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
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(100, 200));
        mipmaps.add(new MockRGBAImage(50, 100));
        mipmaps.add(new MockRGBAImage(25, 50));
        mipmaps.add(new MockRGBAImage(12, 25));
        mipmaps.add(new MockRGBAImage(6, 12));

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
    public void upload_PointOnVerticalBorder_NoneUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(8, 16));
        mipmaps.add(new MockRGBAImage(4, 8));
        mipmaps.add(new MockRGBAImage(2, 4));
        mipmaps.add(new MockRGBAImage(1, 2));
        mipmaps.add(new MockRGBAImage(0, 1));

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(4, 16));

        assertNull(mipmaps.get(0).getLastUploadPoint());
        assertNull(mipmaps.get(1).getLastUploadPoint());
        assertNull(mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointOnHorizontalBorder_NoneUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(8, 16));
        mipmaps.add(new MockRGBAImage(4, 8));
        mipmaps.add(new MockRGBAImage(2, 4));
        mipmaps.add(new MockRGBAImage(1, 2));
        mipmaps.add(new MockRGBAImage(0, 1));

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(8, 8));

        assertNull(mipmaps.get(0).getLastUploadPoint());
        assertNull(mipmaps.get(1).getLastUploadPoint());
        assertNull(mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointOnCorner_NoneUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(8, 16));
        mipmaps.add(new MockRGBAImage(4, 8));
        mipmaps.add(new MockRGBAImage(2, 4));
        mipmaps.add(new MockRGBAImage(1, 2));
        mipmaps.add(new MockRGBAImage(0, 1));

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(8, 16));

        assertNull(mipmaps.get(0).getLastUploadPoint());
        assertNull(mipmaps.get(1).getLastUploadPoint());
        assertNull(mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_MipmapsEmptyWidth_EmptyNotUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(10, 20));
        mipmaps.add(new MockRGBAImage(5, 10));
        mipmaps.add(new MockRGBAImage(2, 5));
        mipmaps.add(new MockRGBAImage(1, 2));
        mipmaps.add(new MockRGBAImage(0, 1));

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
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(20, 10));
        mipmaps.add(new MockRGBAImage(10, 5));
        mipmaps.add(new MockRGBAImage(5, 2));
        mipmaps.add(new MockRGBAImage(2, 1));
        mipmaps.add(new MockRGBAImage(1, 0));

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
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(10, 10));
        mipmaps.add(new MockRGBAImage(5, 5));
        mipmaps.add(new MockRGBAImage(2, 2));
        mipmaps.add(new MockRGBAImage(1, 1));
        mipmaps.add(new MockRGBAImage(0, 0));

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
    public void upload_PointOutsideMipmap_NotUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(10, 10));
        mipmaps.add(new MockRGBAImage(5, 5));
        mipmaps.add(new MockRGBAImage(2, 2));
        mipmaps.add(new MockRGBAImage(1, 1));
        mipmaps.add(new MockRGBAImage(0, 0));

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(500, 500));

        assertNull(mipmaps.get(0).getLastUploadPoint());
        assertNull(mipmaps.get(1).getLastUploadPoint());
        assertNull(mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

    @Test
    public void upload_PointDividesOutsideMipmap_EmptyNotUploaded() {
        List<MockRGBAImage> mipmaps = new ArrayList<>();
        mipmaps.add(new MockRGBAImage(8, 8));
        mipmaps.add(new MockRGBAImage(2, 2));
        mipmaps.add(new MockRGBAImage(1, 1));
        mipmaps.add(new MockRGBAImage(0, 0));
        mipmaps.add(new MockRGBAImage(0, 0));

        RGBAImageFrame frame = new RGBAImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                mipmaps
        );

        frame.uploadAt(new Point(5, 5));

        assertEquals(new Point(5, 5), mipmaps.get(0).getLastUploadPoint());
        assertNull(mipmaps.get(1).getLastUploadPoint());
        assertNull(mipmaps.get(2).getLastUploadPoint());
        assertNull(mipmaps.get(3).getLastUploadPoint());
        assertNull(mipmaps.get(4).getLastUploadPoint());
    }

}