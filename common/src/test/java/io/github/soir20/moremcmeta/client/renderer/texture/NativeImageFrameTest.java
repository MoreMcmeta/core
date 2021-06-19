package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.soir20.moremcmeta.client.io.FrameReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link NativeImageFrame}.
 * @author soir20
 */
public class NativeImageFrameTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_FrameDataNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageFrame(null, new NativeImage[1], false, false, false);
    }

    @Test
    public void construct_MipmapsNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new NativeImageFrame(new FrameReader.FrameData(100, 100, 0, 0, 10),
                null, false, false, false);
    }

    @Test
    public void getMipmapLevel_NoMipmapsProvided_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[0], false, false, false
        );
    }

    @Test
    public void getFrameTime_NotEmptyTime_SameTimeReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, 10),
                new NativeImage[1], false, false, false
        );

        assertEquals(10, frame.getFrameTime());
    }

    @Test
    public void getFrameTime_EmptyTime_SameTimeReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 100, 0, 0, FrameReader.FrameData.EMPTY_TIME),
                new NativeImage[1], false, false, false
        );

        assertEquals(FrameReader.FrameData.EMPTY_TIME, frame.getFrameTime());
    }

    @Test
    public void getWidth_WidthProvided_SameWidthReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                new NativeImage[1], false, false, false
        );

        assertEquals(100, frame.getWidth());
    }

    @Test
    public void getHeight_HeightProvided_SameHeightReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 0, 0, 10),
                new NativeImage[1], false, false, false
        );

        assertEquals(200, frame.getHeight());
    }

    @Test
    public void getXOffset_XOffsetProvided_SameXOffsetReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[1], false, false, false
        );

        assertEquals(30, frame.getXOffset());
    }

    @Test
    public void getYOffset_YOffsetProvided_SameYOffsetReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[1], false, false, false
        );

        assertEquals(40, frame.getYOffset());
    }

    @Test
    public void getMipmapLevel_MipmapsProvided_MipmapLevelReturned() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[5], false, false, false
        );

        assertEquals(4, frame.getMipmapLevel());
    }

    @Test
    public void getMipmap_MipmapAtArrayLength_IllegalArgException() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[5], false, false, false
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(5);
    }

    @Test
    public void getMipmap_MipmapBeyondArrayLength_IllegalArgException() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[5], false, false, false
        );

        expectedException.expect(IllegalArgumentException.class);
        frame.getImage(6);
    }

    @Test
    public void getMipmap_MipmapNull_Null() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[5], false, false, false
        );

        assertNull(frame.getImage(2));
    }

    @Test
    public void upload_NullPoint_NullPointerException() {
        NativeImageFrame frame = new NativeImageFrame(
                new FrameReader.FrameData(100, 200, 30, 40, 10),
                new NativeImage[5], false, false, false
        );

        expectedException.expect(NullPointerException.class);
        frame.uploadAt(null);
    }

}