package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IAnimationFrame<T extends IUploadableMipmap> {

    IMipmappableImage<T> getImage();

    int getFrameTime();

    void uploadAt(int x, int y);

}
