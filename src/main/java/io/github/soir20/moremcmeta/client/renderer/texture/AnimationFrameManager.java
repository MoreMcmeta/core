package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Manages the current frame in an animation.
 * @param <T>   animation frame class
 * @author soir20
 */
public class AnimationFrameManager<T> implements ITickable {
    private final List<T> INTERPOLATED_FRAMES;
    private final Function<T, Integer> FRAME_TIME_CALCULATOR;

    private int ticksInThisFrame;
    private int currentFrame;

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     */
    public AnimationFrameManager(List<T> frames, Function<T, Integer> frameTimeCalculator) {
        this(frameTimeCalculator);

        INTERPOLATED_FRAMES.addAll(frames);
    }

    /**
     * Creates an animation frame manager that interpolates between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     * @param interpolator          interpolates between frames of the animation
     */
    public AnimationFrameManager(List<T> frames, Function<T, Integer> frameTimeCalculator,
                                 BiFunction<T, T, Collection<T>> interpolator) {
        this(frameTimeCalculator);

        int numFrames = frames.size();
        for (int frameIndex = 0; frameIndex < numFrames; frameIndex++) {
            T frame = frames.get(frameIndex);

            if (FRAME_TIME_CALCULATOR.apply(frame) > 1) {
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
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     */
    private AnimationFrameManager(Function<T, Integer> frameTimeCalculator) {
        INTERPOLATED_FRAMES = new ArrayList<>();
        FRAME_TIME_CALCULATOR = frameTimeCalculator;
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

        if (ticksInThisFrame == FRAME_TIME_CALCULATOR.apply(getCurrentFrame())) {
            currentFrame = (currentFrame + 1) % INTERPOLATED_FRAMES.size();
            ticksInThisFrame = 0;
        }
    }

}
