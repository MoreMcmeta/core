package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IAnimationFrame<I extends IUploadableMipmap> {

    IMipmappableImage<I> getImage();

    int getFrameTime();

    void uploadAt(int x, int y);

}
