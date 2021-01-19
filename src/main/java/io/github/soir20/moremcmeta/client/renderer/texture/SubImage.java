package io.github.soir20.moremcmeta.client.renderer.texture;

import java.util.Collection;

public class SubImage<I extends IUploadableMipmap> implements IMipmappableImage<I> {
    private final MipmapContainer<I> MIPMAPS;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int WIDTH;
    private final int HEIGHT;
    private final boolean BLUR;
    private final boolean CLAMP;
    private final boolean AUTO_CLOSE;

    public SubImage(MipmapContainer<I> mipmaps, int xOffset, int yOffset, int width, int height,
                    boolean blur, boolean clamp, boolean autoClose) {
        MIPMAPS = mipmaps;
        X_OFFSET = xOffset;
        Y_OFFSET = yOffset;
        WIDTH = width;
        HEIGHT = height;
        BLUR = blur;
        CLAMP = clamp;
        AUTO_CLOSE = autoClose;
    }

    public I getMipmap(int level) {
        return MIPMAPS.getMipmap(level);
    }

    public boolean isMipmapped() {
        return MIPMAPS.getMipmapLevels().size() > 1;
    }

    public Collection<Integer> getMipmapLevels() {
        return MIPMAPS.getMipmapLevels();
    }

    public void uploadAt(int x, int y) {
        for (int level : getMipmapLevels()) {
            int mipmappedX = x >> level;
            int mipmappedY = y >> level;
            int mipmappedSkipX = X_OFFSET >> level;
            int mipmappedSkipY = Y_OFFSET >> level;
            int mipmappedWidth = WIDTH >> level;
            int mipmappedHeight = HEIGHT >> level;

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                MIPMAPS.getMipmap(level).uploadAt(mipmappedX, mipmappedY, mipmappedSkipX, mipmappedSkipY,
                        mipmappedWidth, mipmappedHeight, level, BLUR, CLAMP, isMipmapped(), AUTO_CLOSE);
            }
        }
    }

}
