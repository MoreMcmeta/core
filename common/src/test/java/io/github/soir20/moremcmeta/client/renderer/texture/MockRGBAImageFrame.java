package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.io.FrameReader;

/**
 * A fake {@link RGBAImageFrame}.
 * @author soir20
 */
public class MockRGBAImageFrame extends RGBAImageFrame {
    private final int FRAME_NUMBER;

    public MockRGBAImageFrame() {
        this(0);
    }

    public MockRGBAImageFrame(int frameNumber) {
        super(new FrameReader.FrameData(10, 10, 0, 0, 1),
                ImmutableList.of(new MockRGBAImage(), new MockRGBAImage(), new MockRGBAImage()));
        FRAME_NUMBER = frameNumber;
    }

    public int getFrameNumber() {
        return FRAME_NUMBER;
    }

}
