package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.concurrent.Executor;

public interface IAnimatedTexture extends AutoCloseable, ITickable {

    void setBlurMipmapDirect(boolean blurIn, boolean mipmapIn);

    void setBlurMipmap(boolean blur, boolean mipmap);

    void restoreLastBlurMipmap();

    int getGlTextureId();

    void deleteGlTexture();

    void loadTexture(IResourceManager manager) throws IOException;

    void bindTexture();

    void loadTexture(TextureManager textureManagerIn, IResourceManager resourceManagerIn,
                     ResourceLocation resourceLocationIn, Executor executorIn);

}
