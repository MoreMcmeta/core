package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Manages the current frame in an animation.
 * @param <F>   animation frame class
 * @author soir20
 */
public class AnimationFrameManager<F> implements ITickable {
    private final List<F> FRAMES;
    private final Function<F, Integer> FRAME_TIME_CALCULATOR;
    private final IInterpolator<F> INTERPOLATOR;

    private int ticksInThisFrame;
    private int currentFrameIndex;
    private F currentFrame;

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     */
    public AnimationFrameManager(List<F> frames, Function<F, Integer> frameTimeCalculator) {
        FRAMES = new ArrayList<>();
        FRAMES.addAll(frames);
        FRAME_TIME_CALCULATOR = frameTimeCalculator;
        INTERPOLATOR = null;
    }

    /**
     * Creates an animation frame manager that interpolates between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     * @param interpolator          interpolates between frames of the animation
     */
    public AnimationFrameManager(List<F> frames, Function<F, Integer> frameTimeCalculator,
                                 IInterpolator<F> interpolator) {
        FRAMES = new ArrayList<>();
        FRAMES.addAll(frames);
        FRAME_TIME_CALCULATOR = frameTimeCalculator;
        INTERPOLATOR = interpolator;
    }

    /**
     * Gets the current frame of the animation.
     * @return  the current frame of the animation
     */
    public F getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Moves the animation forward by one tick.
     */
    @Override
    public void tick() {
        ticksInThisFrame++;

        int maxTime = FRAME_TIME_CALCULATOR.apply(getCurrentFrame());
        int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

        if (INTERPOLATOR != null && ticksInThisFrame < maxTime) {
            currentFrame = INTERPOLATOR.interpolate(maxTime, ticksInThisFrame,
                    currentFrame, FRAMES.get(nextFrameIndex));
        } else if (ticksInThisFrame == maxTime) {
            currentFrameIndex = nextFrameIndex;
            currentFrame = FRAMES.get(nextFrameIndex);
            ticksInThisFrame = 0;
        }
    }

}
