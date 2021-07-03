package io.github.soir20.moremcmeta.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;

/**
 * A fake {@link RGBAImageFrame}.
 * @author soir20
 */
public class MockRGBAImageFrame extends RGBAImageFrame {
    private final int FRAME_NUMBER;
    private int uploads;

    public MockRGBAImageFrame() {
        this(0);
    }

    public MockRGBAImageFrame(int frameNumber) {
        super(new FrameReader.FrameData(10, 10, 0, 0, 1),
                ImmutableList.of(new MockRGBAImage(), new MockRGBAImage(), new MockRGBAImage()));
        FRAME_NUMBER = frameNumber;
    }

    public MockRGBAImageFrame(int width, int height) {
        this(width, height, 3);
    }

    public MockRGBAImageFrame(int width, int height, int mipmap) {
        super(new FrameReader.FrameData(width, height, 0, 0, 1), createMipmaps(mipmap, width, height));
        FRAME_NUMBER = 0;
    }

    @Override
    public void uploadAt(Point point) {
        super.uploadAt(point);
        uploads++;
    }

    public int getFrameNumber() {
        return FRAME_NUMBER;
    }

    public int getUploadCount() {
        return uploads;
    }

    private static ImmutableList<IRGBAImage> createMipmaps(int mipmap, int width, int height) {
        ImmutableList.Builder<IRGBAImage> builder = new ImmutableList.Builder<>();

        for (int level = 0; level <= mipmap; level++) {
            builder.add(new MockRGBAImage(width >> level, height >> level));
        }

        return builder.build();
    }

}
