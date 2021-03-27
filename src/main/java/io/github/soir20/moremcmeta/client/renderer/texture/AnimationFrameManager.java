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

    /**
     * Creates an animation frame manager that does not interpolate between frames.
     * @param frames                frames of the animation
     * @param frameTimeCalculator   calculates the frame time for a given frame.
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

        if (frames.stream().anyMatch((frame) -> frameTimeCalculator.applyAsInt(frame) <= 0)) {
            throw new IllegalArgumentException("Each frame must be at least one tick long");
        }
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
    public AnimationFrameManager(ImmutableList<F> frames, ToIntFunction<F> frameTimeCalculator,
                                 IInterpolator<F> interpolator) {
        FRAMES = requireNonNull(frames, "Frames cannot be null");
        FRAME_TIME_CALCULATOR = requireNonNull(frameTimeCalculator, "Frame time calculator cannot be null");
        INTERPOLATOR = requireNonNull(interpolator, "Interpolator cannot be null");

        if (frames.size() == 0) {
            throw new IllegalArgumentException("Frames cannot have no frames");
        }

        if (frames.stream().anyMatch((frame) -> frameTimeCalculator.applyAsInt(frame) <= 0)) {
            throw new IllegalArgumentException("Each frame must be at least one tick long");
        }
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

            int maxTime = FRAME_TIME_CALCULATOR.applyAsInt(currentPredefinedFrame);
            int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

            currentFrame = INTERPOLATOR.interpolate(maxTime, ticksInThisFrame, currentPredefinedFrame,
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

        F currentPredefinedFrame = FRAMES.get(currentFrameIndex);

        int maxTime = FRAME_TIME_CALCULATOR.applyAsInt(currentPredefinedFrame);
        int nextFrameIndex = (currentFrameIndex + 1) % FRAMES.size();

        if (ticksInThisFrame >= maxTime) {
            currentFrameIndex = nextFrameIndex;
            ticksInThisFrame = 0;
        }
    }

}
