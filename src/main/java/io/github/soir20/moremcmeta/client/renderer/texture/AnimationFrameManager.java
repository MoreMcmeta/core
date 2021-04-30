package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.ToIntFunction;

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
    private final ImmutableList<F> FRAMES;
    private final ToIntFunction<F> FRAME_TIME_CALCULATOR;

    @Nullable
    private final IInterpolator<F> INTERPOLATOR;

    private int ticksInThisFrame;
    private int currentFrameIndex;
    private int currentFrameMaxTime;

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames                frames of the animation. Must not be empty.
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              Only called once per frame per loop of the animation.
     *                              Must return values greater than 0 for all frames.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     */
    public AnimationFrameManager(ImmutableList<F> frames, ToIntFunction<F> frameTimeCalculator) {
        FRAMES = requireNonNull(frames, "Frames cannot be null");
        FRAME_TIME_CALCULATOR = requireNonNull(frameTimeCalculator, "Frame time calculator cannot be null");
        INTERPOLATOR = null;

        if (frames.size() == 0) {
            throw new IllegalArgumentException("Frames cannot have no frames");
        }

        currentFrameMaxTime = calcMaxFrameTime(0);
    }

    /**
     * Creates an animation frame manager that interpolates between frames.
     * @param frames                frames of the animation. Must not be empty.
     * @param frameTimeCalculator   calculates the frame time for a given frame.
     *                              Only called once per frame per loop of the animation.
     *                              Must return values greater than 0 for all frames.
     *                              In most cases, pass a function that gets the
     *                              time from the frame or returns a default value.
     * @param interpolator          interpolates between frames of the animation
     */
    public AnimationFrameManager(ImmutableList<F> frames, ToIntFunction<F> frameTimeCalculator,
                                 IInterpolator<F> interpolator) {
        FRAMES = requireNonNull(frames, "Frames cannot be null");
        FRAME_TIME_CALCULATOR = requireNonNull(frameTimeCalculator, "Frame time calculator cannot be null");
        INTERPOLATOR = requireNonNull(interpolator, "Interpolator cannot be null");

        if (frames.size() == 0) {
            throw new IllegalArgumentException("Frames cannot have no frames");
        }

        currentFrameMaxTime = calcMaxFrameTime(0);
    }

    /**
     * Gets the current frame of the animation, which may be an interpolated frame.
     * @return  the current frame of the animation
     */
    public F getCurrentFrame() {
        F currentPredefinedFrame = FRAMES.get(currentFrameIndex);
        F currentFrame = currentPredefinedFrame;

        // Doing interpolation when the frame is retrieved ensures we don't interpolate when the frame isn't used
        if (ticksInThisFrame > 0 && INTERPOLATOR != null) {
            int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();
            currentFrame = INTERPOLATOR.interpolate(currentFrameMaxTime, ticksInThisFrame, currentPredefinedFrame,
                    FRAMES.get(nextFrameIndex));
        }

        return currentFrame;
    }

    /**
     * Moves the animation forward by one tick. Does not perform interpolation. Interpolation happens
     * when {@link #getCurrentFrame()} is used to retrieve the current animation frame.
     */
    @Override
    public void tick() {
        ticksInThisFrame++;
        int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

        if (ticksInThisFrame >= currentFrameMaxTime) {
            currentFrameIndex = nextFrameIndex;
            ticksInThisFrame = 0;
            currentFrameMaxTime = calcMaxFrameTime(currentFrameIndex);
        }
    }

    /**
     * Ticks the current animation by several ticks. Identical to {@link #tick()} for single-tick
     * animation updates. Like {@link #tick()}, this method does not perform interpolation.
     * Interpolation happens when {@link #getCurrentFrame()} is used to retrieve the current
     * animation frame.
     * @param ticks      how many ticks ahead to put the animation
     */
    public void tick(int ticks) {

        // Calculate the predefined frame in the animation at the given tick
        int timeLeftUntilTick = ticksInThisFrame + ticks;
        int frameIndex = currentFrameIndex;
        int frameTime = currentFrameMaxTime;

        // When the frame time is equal to the time left, the tick is at the start of the next frame
        while (frameTime <= timeLeftUntilTick) {
            timeLeftUntilTick -= frameTime;
            frameIndex = (frameIndex + 1) % FRAMES.size();
            frameTime = calcMaxFrameTime(frameIndex);
        }

        currentFrameIndex = frameIndex;
        ticksInThisFrame = timeLeftUntilTick;
    }

    /**
     * Calculates the maximum time for a frame at a certain index.
     * @param frameIndex    the index of the frame
     * @return  the maximum time of this frame
     */
    private int calcMaxFrameTime(int frameIndex) {
        int maxTime = FRAME_TIME_CALCULATOR.applyAsInt(FRAMES.get(frameIndex));

        if (maxTime <= 0) {
            throw new UnsupportedOperationException("Frame times must be greater than 0");
        }

        return maxTime;
    }

}
