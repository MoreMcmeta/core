package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

public class NativeImageFrame implements IAnimationFrame {
    private final NativeImage[] MIPMAPS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int FRAME_TIME;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final boolean AUTO_CLOSE;

    public NativeImageFrame(FrameReader.FrameData frameData, NativeImage[] mipmaps,
                            boolean blur, boolean clamp, boolean autoClose) {
        MIPMAPS = mipmaps;
        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        FRAME_TIME = frameData.getTime();
        BLUR = blur;
        CLAMP = clamp;
        AUTO_CLOSE = autoClose;
    }

    public void uploadAt(int x, int y) {
        for (int level = 0; level < MIPMAPS.length; level++) {
            int mipmappedX = x >> level;
            int mipmappedY = y >> level;
            int mipmappedSkipX = X_OFFSET >> level;
            int mipmappedSkipY = Y_OFFSET >> level;
            int mipmappedWidth = WIDTH >> level;
            int mipmappedHeight = HEIGHT >> level;

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                MIPMAPS[level].uploadTextureSub(level, mipmappedX, mipmappedY, mipmappedSkipX, mipmappedSkipY,
                        mipmappedWidth, mipmappedHeight, BLUR, CLAMP, MIPMAPS.length > 1, AUTO_CLOSE);
            }
        }
    }

    public int getFrameTime() {
        return FRAME_TIME;
    }

    public NativeImageRGBAWrapper getImageWrapper(int level) {
        return new NativeImageRGBAWrapper(MIPMAPS[level], X_OFFSET >> level, Y_OFFSET >> level);
    }

}
