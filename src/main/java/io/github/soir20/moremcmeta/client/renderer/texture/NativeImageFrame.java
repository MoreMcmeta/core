package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationFrame;

import java.util.Set;

public class NativeImageFrame extends AnimationFrame {
    private final MipmappedNativeImage IMAGE;

    public NativeImageFrame(int frameIndexIn, MipmappedNativeImage image) {
        super(frameIndexIn);
        IMAGE = image;
    }

    public NativeImageFrame(int frameIndexIn, int frameTimeIn, MipmappedNativeImage image) {
        super(frameIndexIn, frameTimeIn);
        IMAGE = image;
    }

    public void upload(int xPos, int yPos, int skipX, int skipY,
                       boolean blur, boolean clamp, boolean autoClose) {
        Set<Integer> levels = IMAGE.getMipmapLevels();
        boolean shouldMipmap = levels.size() > 1;

        for (int level : levels) {
            NativeImage image = IMAGE.getMipmap(level);

            int width = getMipmappedDimension(level, image.getWidth());
            int height = getMipmappedDimension(level, image.getWidth());

            // Uploading an empty image is a waste of time
            if (width > 0 && height > 0) {
                int x = getMipmappedDimension(level, xPos);
                int y = getMipmappedDimension(level, yPos);
                int unpackSkipPixels = getMipmappedDimension(level, skipX);
                int unpackSkipRows = getMipmappedDimension(level, skipY);

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
