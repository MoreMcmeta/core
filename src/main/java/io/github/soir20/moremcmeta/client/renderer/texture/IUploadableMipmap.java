package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IUploadableMipmap {
    void uploadAt(int x, int y, int skipX, int skipY, int widthIn, int heightIn, int mipmap,
                  boolean blur, boolean clamp, boolean shouldMipmap, boolean autoClose);
}
