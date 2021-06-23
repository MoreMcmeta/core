package io.github.soir20.moremcmeta.client.renderer.texture;

import io.github.soir20.moremcmeta.client.io.FrameReader;
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

}