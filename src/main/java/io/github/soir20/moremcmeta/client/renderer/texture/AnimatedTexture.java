package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An animated texture that an update on tick.
 * @author soir20
 */
@ParametersAreNonnullByDefault
public class AnimatedTexture extends Texture implements ITickable {
    private final AnimationFrameManager<NativeImageFrame> FRAME_MANAGER;
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;
    private final int MIPMAP;

    /**
     * Creates a new animated texture.
     *
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmap                    mipmap levels for all frames
     */
    public AnimatedTexture(AnimationFrameManager<NativeImageFrame> frameManager,
                           int frameWidth, int frameHeight, int mipmap) {
        FRAME_MANAGER = frameManager;
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmap;
    }

    /**
     * Uploads this texture to OpenGL on the appropriate thread.
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
     * Updates this texture's animation on the appropriate thread each tick.
     */
    @Override
    public void tick() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::updateAnimation);
        } else {
            updateAnimation();
        }
    }

    /**
     * Updates this texture's animation immediately.
     */
    private void updateAnimation() {
        FRAME_MANAGER.tick();

        bindTexture();
        uploadCurrentFrame();
    }

    /**
     * Uploads the current frame immediately.
     */
    private void uploadCurrentFrame() {
        FRAME_MANAGER.getCurrentFrame().uploadAt(0, 0);
    }

}
