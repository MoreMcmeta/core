package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.resources.data.AnimationFrame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class AnimationFrameManager<T extends AnimationFrame> implements ITickable {
    private final List<T> INTERPOLATED_FRAMES;
    private final int DEFAULT_FRAME_TIME;

    private int ticksInThisFrame;
    private int currentFrame;

    public AnimationFrameManager(List<T> frames, int defaultFrameTime) {
        this(defaultFrameTime);

        INTERPOLATED_FRAMES.addAll(frames);
    }

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

    private AnimationFrameManager(int defaultFrameTime) {
        INTERPOLATED_FRAMES = new ArrayList<>();
        DEFAULT_FRAME_TIME = defaultFrameTime;
    }

    public T getCurrentFrame() {
        return INTERPOLATED_FRAMES.get(currentFrame);
    }

    @Override
    public void tick() {
        ticksInThisFrame++;

        if (ticksInThisFrame == getFrameTime(getCurrentFrame())) {
            currentFrame = (currentFrame + 1) % INTERPOLATED_FRAMES.size();
            ticksInThisFrame = 0;
        }
    }

    private int getFrameTime(T frame) {
        return frame.getFrameTime() == -1 ? DEFAULT_FRAME_TIME : frame.getFrameTime();
    }
}
