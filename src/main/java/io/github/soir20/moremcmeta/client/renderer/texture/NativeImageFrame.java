package io.github.soir20.moremcmeta.client.renderer.texture;

public class NativeImageFrame implements IAnimationFrame<NativeImageRGBAWrapper> {
    private final SubImage<NativeImageRGBAWrapper> IMAGE;
    private final int FRAME_TIME;

    public NativeImageFrame(SubImage<NativeImageRGBAWrapper> image, int frameTime) {
        IMAGE = image;
        FRAME_TIME = frameTime;
    }

    public void uploadAt(int x, int y) {
        IMAGE.uploadAt(x, y);
    }

    public int getFrameTime() {
        return FRAME_TIME;
    }

    public SubImage<NativeImageRGBAWrapper> getImage() {
        return IMAGE;
    }

}
