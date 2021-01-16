package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.NativeImage;

public class NativeImageRGBAWrapper implements IRGBAImage {
    private final NativeImage IMAGE;

    public NativeImageRGBAWrapper(NativeImage image) {
        IMAGE = image;
    }

    @Override
    public int getPixel(int x, int y) {
        return IMAGE.getPixelRGBA(x, y);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        IMAGE.setPixelRGBA(x, y, color);
    }

    @Override
    public int getWidth() {
        return IMAGE.getWidth();
    }

    @Override
    public int getHeight() {
        return IMAGE.getHeight();
    }
}
