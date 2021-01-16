package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.data.AnimationFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Manages the current frame in an animation.
 * @param <T>   animation frame class
 * @author soir20
 */
public class AnimationFrameManager<T extends AnimationFrame> implements ITickable {
    private final List<T> INTERPOLATED_FRAMES;
    private final int DEFAULT_FRAME_TIME;

    private int ticksInThisFrame;
    private int currentFrame;

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames            frames of the animation
     * @param defaultFrameTime  default frame time if a frame has no time
     */
    public AnimationFrameManager(List<T> frames, int defaultFrameTime) {
        this(defaultFrameTime);

        INTERPOLATED_FRAMES.addAll(frames);
    }

    /**
     * Creates an animation frame manager that interpolates between frames.
     * @param frames            frames of the animation
     * @param defaultFrameTime  default frame time if a frame has no time
     * @param interpolator      interpolates between frames of the animation
     */
    public AnimationFrameManager(List<T> frames, int defaultFrameTime,
                                 BiFunction<T, T, Collection<T>> interpolator) {
        this(defaultFrameTime);

        int numFrames = frames.size();
        for (int frameIndex = 0; frameIndex < numFrames; frameIndex++) {
            T frame = frames.get(frameIndex);

            if (getFrameTime(frame) > 1) {
                T nextFrame = frames.get((frameIndex + 1) % numFrames);
                Collection<T> intermediateFrames = interpolator.apply(frame, nextFrame);

                INTERPOLATED_FRAMES.add(frame);
                INTERPOLATED_FRAMES.addAll(intermediateFrames);
            } else {
                INTERPOLATED_FRAMES.add(frame);
            }
        }
    }

    /**
     * Initializes common fields in a newly-created animation frame manager.
     * @param defaultFrameTime  default frame time if a frame has no time
     */
    private AnimationFrameManager(int defaultFrameTime) {
        INTERPOLATED_FRAMES = new ArrayList<>();
        DEFAULT_FRAME_TIME = defaultFrameTime;
    }

    /**
     * Gets the current frame of the animation.
     * @return  the current frame of the animation
     */
    public T getCurrentFrame() {
        return INTERPOLATED_FRAMES.get(currentFrame);
    }

    /**
     * Moves the animation forward by one tick.
     */
    @Override
    public void tick() {
        ticksInThisFrame++;

        if (ticksInThisFrame == getFrameTime(getCurrentFrame())) {
            currentFrame = (currentFrame + 1) % INTERPOLATED_FRAMES.size();
            ticksInThisFrame = 0;
        }
    }

    /**
     * Gets a frame's length or the default time if it has none.
     * @param frame     the frame to get the length of
     * @return  the length of the current frame
     */
    private int getFrameTime(T frame) {
        return frame.getFrameTime() == -1 ? DEFAULT_FRAME_TIME : frame.getFrameTime();
    }

}
