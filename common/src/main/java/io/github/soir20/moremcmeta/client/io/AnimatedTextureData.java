package io.github.soir20.moremcmeta.client.io;

import io.github.soir20.moremcmeta.client.animation.AnimationFrameManager;
import io.github.soir20.moremcmeta.client.animation.IAnimationFrame;

import static java.util.Objects.requireNonNull;

/**
 * Holds the outputs of animation parsing before they are attached to a texture.
 * @param <F>   animation frame type
 * @author soir20
 */
public class AnimatedTextureData<F extends IAnimationFrame> {
    private static final int EMPTY_SYNC_TICKS = -1;

    private final boolean DO_TIME_SYNC;
    private final int SYNC_TICKS;

    private final AnimationFrameManager<F> FRAME_MANAGER;
    private final int FRAME_WIDTH;
    private final int FRAME_HEIGHT;
    private final int MIPMAP;
    private final Runnable CLOSE_ACTION;

    /**
     * Creates a container for data about an animated texture that syncs to game time.
     * @param syncTicks                 number of in-game ticks to sync the animation to
     * @param frameManager              manages the frames of the texture's animation
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmapLevel               mipmap levels for all frames
     * @param closeAction               closes the frames in the animated texture
     */
    public AnimatedTextureData(int syncTicks, AnimationFrameManager<F> frameManager, int frameWidth,
                               int frameHeight, int mipmapLevel, Runnable closeAction) {
        if (syncTicks <= 0) {
            throw new IllegalArgumentException("Animation cannot sync to zero or fewer ticks");
        }
        DO_TIME_SYNC = true;
        SYNC_TICKS = syncTicks;

        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmapLevel;
        CLOSE_ACTION = requireNonNull(closeAction, "Close action cannot be null");
    }

    /**
     * Creates a container for data about an animated texture that does not sync to game time.
     * @param frameManager              manages the frames of the texture's animation
     * @param frameWidth                width of a single frame (same for all frames)
     * @param frameHeight               height of a single frame (same for all frames)
     * @param mipmapLevel               mipmap levels for all frames
     * @param closeAction               closes the frames in the animated texture
     */
    public AnimatedTextureData(AnimationFrameManager<F> frameManager, int frameWidth,
                               int frameHeight, int mipmapLevel, Runnable closeAction) {
        DO_TIME_SYNC = false;
        SYNC_TICKS = EMPTY_SYNC_TICKS;

        FRAME_MANAGER = requireNonNull(frameManager, "Frame manager cannot be null");
        FRAME_WIDTH = frameWidth;
        FRAME_HEIGHT = frameHeight;
        MIPMAP = mipmapLevel;
        CLOSE_ACTION = closeAction;
    }

    /**
     * Gets whether the texture synchronizes to game time.
     * @return whether the texture synchronizes to game time
     */
    public boolean isTimeSynchronized() {
        return DO_TIME_SYNC;
    }

    /**
     * Gets the number of ticks the texture synchronizes to. This method is not guaranteed
     * to return any value when the texture is not synchronized. Use
     * {@link #isTimeSynchronized()} to determine if the texture is synchronized.
     * @return the number of ticks the texture synchronizes to
     */
    public int getSynchronizedTicks() {
        return SYNC_TICKS;
    }

    /**
     * Gets the frame manager with all animation frames.
     * @return the frame manager for the texture
     */
    public AnimationFrameManager<F> getFrameManager() {
        return FRAME_MANAGER;
    }

    /**
     * Gets the width of a single frame in the animation.
     * @return the width of one frame in the animation
     */
    public int getFrameWidth() {
        return FRAME_WIDTH;
    }

    /**
     * Gets the height of a single frame in the animation.
     * @return the height of one frame in the animation
     */
    public int getFrameHeight() {
        return FRAME_HEIGHT;
    }

    /**
     * Gets the mipmap level of the animated texture.
     * @return the texture's mipmap level
     */
    public int getMipmapLevel() {
        return MIPMAP;
    }

    /**
     * Gets the close action for this animated texture.
     * @return the close action for this texture
     */
    public Runnable getCloseAction() {
        return CLOSE_ACTION;
    }

}
