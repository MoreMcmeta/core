package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationFrame;

import java.util.Set;

public class NativeImageFrame extends AnimationFrame {
    private final MipmappedNativeImage IMAGE;
    private final int X_POS;
    private final int Y_POS;
    private final int SKIP_X;
    private final int SKIP_Y;

    public NativeImageFrame(int frameIndexIn, MipmappedNativeImage image,
                            int xPos, int yPos, int skipX, int skipY) {
        super(frameIndexIn);
        IMAGE = image;
        X_POS = xPos;
        Y_POS = yPos;
        SKIP_X = skipX;
        SKIP_Y = skipY;
    }

    public NativeImageFrame(int frameIndexIn, MipmappedNativeImage image,
                            int xPos, int yPos, int skipX, int skipY, int frameTimeIn) {
        super(frameIndexIn, frameTimeIn);
        IMAGE = image;
        X_POS = xPos;
        Y_POS = yPos;
        SKIP_X = skipX;
        SKIP_Y = skipY;
    }

    public void upload(boolean blur, boolean clamp, boolean autoClose) {
        Set<Integer> levels = IMAGE.getMipmapLevels();
        boolean shouldMipmap = levels.size() > 1;

        for (int level : levels) {
            NativeImage image = IMAGE.getMipmap(level);

            int width = getMipmappedDimension(level, image.getWidth());
            int height = getMipmappedDimension(level, image.getWidth());

            // Uploading an empty image is a waste of time
            if (width > 0 && height > 0) {
                int x = getMipmappedDimension(level, X_POS);
                int y = getMipmappedDimension(level, Y_POS);
                int unpackSkipPixels = getMipmappedDimension(level, SKIP_X);
                int unpackSkipRows = getMipmappedDimension(level, SKIP_Y);

                IMAGE.getMipmap(level).uploadTextureSub(level, x, y, unpackSkipPixels, unpackSkipRows,
                        width, height, blur, clamp, shouldMipmap, autoClose);
            }

        }
    }

    private int getMipmappedDimension(int level, int value) {

        // Use bitwise shift to divide by a power of two to avoid rounding Math.pow(2, level)
        return value >> level;

    }

}
