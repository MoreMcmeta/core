package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IRGBAImage {

    int getPixel(int x, int y);

    void setPixel(int x, int y, int color);

    int getWidth();

    int getHeight();
}
