package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureData;

/**
 * An animated texture that is not part of an atlas.
 * @param <F>   animation frame type
 * @author soir20
 */
public class SingleAnimatedTexture<F extends IAnimationFrame> extends AnimatedTexture<F> {

    /**
     * Creates a new animated texture that is not part of an atlas.
     * @param data          texture data
     */
    public SingleAnimatedTexture(AnimatedTextureData<F> data) {
        super(data);
    }

    /**
     * Binds this texture to OpenGL for rendering. Interpolation (if used) occurs at this point as well.
     */
    @Override
    public void bind() {
        super.bind();
        uploadCurrentFrame();
    }

    /**
     * Uploads this image to OpenGL immediately.
     */
    @Override
    protected void loadImage() {
        AnimatedTextureData<F> data = getData();
        TextureUtil.prepareImage(getId(), data.getMipmapLevel(), data.getFrameWidth(), data.getFrameHeight());
        uploadCurrentFrame();
    }

    /**
     * Uploads the current frame immediately.
     */
    private void uploadCurrentFrame() {
        getData().getFrameManager().getCurrentFrame().uploadAt(0, 0);
    }

}
