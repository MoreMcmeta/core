package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Manages the current frame in an animation and, optionally, handles the creation of interpolated frames.
 * Interpolation only occurs when an interpolated frame is requested.
 * @param <F>   animation frame type
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AnimationFrameManager<F> implements ITickable {
    private final List<F> FRAMES;
    private final Function<F, Integer> FRAME_TIME_CALCULATOR;

    @Nullable
    private final IInterpolator<F> INTERPOLATOR;

    private int ticksInThisFrame;
    private int currentFrameIndex;
    private F currentFrame;
    private boolean doInterpolation;

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     */
    public AnimationFrameManager(ImmutableList<F> frames, Function<F, Integer> frameTimeCalculator) {
        requireNonNull(frames, "Frames cannot be null");
        FRAMES = frames;

        requireNonNull(frameTimeCalculator, "Frame time calculator cannot be null");
        FRAME_TIME_CALCULATOR = frameTimeCalculator;
        INTERPOLATOR = null;
        currentFrame = FRAMES.get(0);
    }

    /**
     * Creates an animation frame manager that interpolates between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     *                              Cannot return null.
     * @param interpolator          interpolates between frames of the animation
     */
    public AnimationFrameManager(ImmutableList<F> frames, Function<F, Integer> frameTimeCalculator,
                                 @Nullable IInterpolator<F> interpolator) {
        requireNonNull(frames, "Frames cannot be null");
        FRAMES = new ArrayList<>();
        FRAMES.addAll(frames);

        requireNonNull(frameTimeCalculator, "Frame time calculator cannot be null");
        FRAME_TIME_CALCULATOR = frameTimeCalculator;
        INTERPOLATOR = interpolator;
        currentFrame = FRAMES.get(0);
    }

    /**
     * Gets the current frame of the animation, which may be an interpolated frame.
     * @return  the current frame of the animation
     */
    public F getCurrentFrame() {

        // Doing interpolation when the frame is retrieved ensures we don't interpolate when the frame isn't used
        if (doInterpolation && INTERPOLATOR != null) {
            F currentPredefinedFrame = FRAMES.get(currentFrameIndex);

            Integer maxTime = FRAME_TIME_CALCULATOR.apply(currentPredefinedFrame);
            requireNonNull(maxTime, "Time for a frame cannot be null");

            int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

            currentFrame = INTERPOLATOR.interpolate(maxTime, ticksInThisFrame, currentPredefinedFrame,
                    FRAMES.get(nextFrameIndex));
            doInterpolation = false;
        }

        return currentFrame;
    }

    /**
     * Moves the animation forward by one tick.
     */
    @Override
    public void tick() {
        ticksInThisFrame++;

        F currentPredefinedFrame = FRAMES.get(currentFrameIndex);

        Integer maxTime = FRAME_TIME_CALCULATOR.apply(currentPredefinedFrame);
        requireNonNull(maxTime, "Time for a frame cannot be null");

        int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

        if (ticksInThisFrame >= maxTime) {
            currentFrameIndex = nextFrameIndex;
            currentFrame = FRAMES.get(nextFrameIndex);
            ticksInThisFrame = 0;
            doInterpolation = false;
        } else {
            doInterpolation = true;
        }
    }

}
