package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;
import io.github.soir20.moremcmeta.client.io.AnimatedTextureData;

import static java.util.Objects.requireNonNull;

/**
 * An animated texture that is not part of an atlas.
 * @param <F>   animation frame type
 * @author soir20
 */
public class SingleAnimatedTexture<F extends IAnimationFrame> extends AnimatedTexture<F> {
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a new animated texture that is not part of an atlas.
     * @param data          texture data
     * @param closeAction   cleans up the texture when it is closed
     */
    public SingleAnimatedTexture(AnimatedTextureData<F> data, Runnable closeAction) {
        super(data);
        CLOSE_ACTION = requireNonNull(closeAction, "Close action cannot be null");
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

    /**
     * Closes all resources that this texture uses.
     */
    @Override
    public void close() {
        CLOSE_ACTION.run();
    }

}
