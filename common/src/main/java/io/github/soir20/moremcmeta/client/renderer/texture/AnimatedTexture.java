package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;
import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.resources.ResourceManager;

import static java.util.Objects.requireNonNull;

/**
 * An animated texture that updates on each tick.
 * @param <F>   animation frame type
 * @author soir20
 */
public class AnimatedTexture<F extends IAnimationFrame> extends AbstractTexture
        implements Tickable, AutoCloseable {
    private final boolean DO_TIME_SYNC;
    private final int SYNC_TICKS;
    private final AnimationFrameManager<F> FRAME_MANAGER;
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;
    private final int MIPMAP;
    private final Runnable CLOSE_ACTION;

    private int ticks;

    /**
     * Creates a new animated texture that syncs to the game time of the current world.
     * @param syncTicks                 number of in-game ticks to sync the animation to
     * @param frameManager              manages the frames of the texture's animation
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmap                    mipmap levels for all frames
     * @param closeAction               cleans up the texture when it is closed
     */
    public AnimatedTexture(int syncTicks, AnimationFrameManager<F> frameManager,
                           int frameWidth, int frameHeight, int mipmap, Runnable closeAction) {
        if (syncTicks <= 0) {
            throw new IllegalArgumentException("Animation cannot sync to zero or fewer ticks");
        }
        DO_TIME_SYNC = true;
        SYNC_TICKS = syncTicks;

        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmap;
        CLOSE_ACTION = requireNonNull(closeAction, "Close action cannot be null");
    }

    /**
     * Creates a new animated texture that does not sync to the game time of the current world.
     * @param frameManager              manages the frames of the texture's animation
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmap                    mipmap levels for all frames
     * @param closeAction               cleans up the texture when it is closed
     */
    public AnimatedTexture(AnimationFrameManager<F> frameManager,
                           int frameWidth, int frameHeight, int mipmap, Runnable closeAction) {
        DO_TIME_SYNC = false;
        SYNC_TICKS = -1;

        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmap;
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
     * Uploads this texture to OpenGL on the rendering thread.
     * @param manager   resource manager
     */
    @Override
    public void load(ResourceManager manager) {
        requireNonNull(manager, "Resource manager cannot be null");

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
        TextureUtil.prepareImage(getId(), MIPMAP, FRAME_WIDTH, FRAME_HEIGHT);
        uploadCurrentFrame();
    }

    /**
     * Updates this texture's animation each tick. The frames are not uploaded until they are used
     * in {@link #bind()}.
     */
    @Override
    public void tick() {
        ClientLevel currentWorld = Minecraft.getInstance().level;

        if (DO_TIME_SYNC && currentWorld != null) {
            int ticksToAdd = (int) ((currentWorld.getDayTime() - ticks) % SYNC_TICKS + SYNC_TICKS) % SYNC_TICKS;
            ticks += ticksToAdd;
            FRAME_MANAGER.tick(ticksToAdd);
        } else {
            ticks++;
            FRAME_MANAGER.tick();
        }
    }

    /**
     * Uploads the current frame immediately. The frame is always uploaded with (0, 0) as the top left coordinate
     * of the frame since we cannot use an atlas without making invasive changes to Minecraft's code. (0, 0)
     * is consistent with the texture UV coordinates used throughout all of Minecraft's code.
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
