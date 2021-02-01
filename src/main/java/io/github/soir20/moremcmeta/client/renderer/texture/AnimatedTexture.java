package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An animated texture that updates on each tick.
 * @param <F>   animation frame type
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class AnimatedTexture<F extends IAnimationFrame> extends Texture implements ITickable, AutoCloseable {
    private final AnimationFrameManager<F> FRAME_MANAGER;
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;
    private final int MIPMAP;
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a new animated texture.
     * @param frameManager              manages the frames of the texture's animation
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmap                    mipmap levels for all frames
     */
    public AnimatedTexture(AnimationFrameManager<F> frameManager,
                           int frameWidth, int frameHeight, int mipmap, Runnable closeAction) {
        FRAME_MANAGER = frameManager;
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmap;
        CLOSE_ACTION = closeAction;
    }

    /**
     * Binds this texture to OpenGL for rendering. Interpolation (if used) occurs at this point as well.
     */
    @Override
    public void bindTexture() {
        super.bindTexture();
        uploadCurrentFrame();
    }

    /**
     * Uploads this texture to OpenGL on the rendering thread.
     * @param manager   resource manager
     */
    @Override
    public void loadTexture(IResourceManager manager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::loadImage);
        } else {
            loadImage();
        }
    }

    /**
     * Uploads this image to OpenGL immediately.
     */
    private void loadImage() {
        TextureUtil.prepareImage(getGlTextureId(), MIPMAP, FRAME_WIDTH, FRAME_HEIGHT);
        uploadCurrentFrame();
    }

    /**
     * Updates this texture's animation each tick. The frames are not uploaded until they are used
     * in {@link #bindTexture()}.
     */
    @Override
    public void tick() {
        FRAME_MANAGER.tick();
    }

    /**
     * Uploads the current frame immediately.
     */
    private void uploadCurrentFrame() {
        FRAME_MANAGER.getCurrentFrame().uploadAt(0, 0);
    }

    /**
     * Closes all resources that this texture uses.
     */
    @Override
    public void close() {
        CLOSE_ACTION.run();
    }

}
