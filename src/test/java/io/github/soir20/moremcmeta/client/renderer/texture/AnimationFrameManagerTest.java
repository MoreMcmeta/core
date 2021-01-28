package io.github.soir20.moremcmeta.client.renderer.texture;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link AnimationFrameManager}.
 * @author soir20
 */
public class AnimationFrameManagerTest {

    @Test
    public void tickAnimation_NoTicks_FirstFrame() {
        List<Integer> mockFrames = new ArrayList<>();
        for (int frame = 0; frame < 5; frame++) {
            mockFrames.add(frame);
        }

        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        assertEquals(new Integer(0), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimation_MaxTicksInEachFrame_FrameChanges() {
        List<Integer> mockFrames = new ArrayList<>();
        for (int frame = 0; frame < 5; frame++) {
            mockFrames.add(frame);
        }

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);

        for (int nextFrame = 1; nextFrame < mockFrames.size(); nextFrame++) {
            for (int tick = 0; tick < frameLength; tick++) {
                manager.tick();
            }
            assertEquals(new Integer(nextFrame), manager.getCurrentFrame());
        }
    }

    @Test
    public void tickAnimation_FrameTimeVaries_SecondFrameLastsCorrectTime() {
        List<Integer> mockFrames = new ArrayList<>();
        for (int frame = 1; frame < 6; frame++) {
            mockFrames.add(frame);
        }

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames,
                (frame) -> frame * frameLength);

        for (int tick = 0; tick < frameLength * 3; tick++) {
            manager.tick();
        }
        assertEquals(new Integer(3), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimation_MaxTicksInAnimation_AnimationResetsAtBeginning() {
        List<Integer> mockFrames = new ArrayList<>();
        for (int frame = 1; frame < 6; frame++) {
            mockFrames.add(frame);
        }

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        for (int tick = 0; tick < frameLength * mockFrames.size(); tick++) {
            manager.tick();
        }

        assertEquals(new Integer(1), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimation_NoInterpolator_NoInterpolatedFrames() {
        List<Integer> mockFrames = new ArrayList<>();
        for (int frame = 1; frame < 6; frame++) {
            mockFrames.add(frame);
        }

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        for (int tick = 1; tick < frameLength; tick++) {
            assertEquals(new Integer(1), manager.getCurrentFrame());
        }
    }

    @Test
    public void tickAnimation_WithInterpolator_InterpolatedFrames() {
        List<String> mockFrames = new ArrayList<>();
        for (int frame = 0; frame < 5; frame++) {
            mockFrames.add(String.valueOf(frame));
        }

        IInterpolator<String> interpolator = ((steps, step, start, end) ->
                String.format("steps: %s, step: %s, start: %s, end: %s", steps, step, start, end)
        );

        int frameLength = 10;
        AnimationFrameManager<String> manager = new AnimationFrameManager<>(mockFrames,
                (frame) -> 10, interpolator);

        for (int frame = 0; frame < mockFrames.size(); frame++) {
            for (int tick = 1; tick < frameLength; tick++) {
                manager.tick();
                String interpolated = String.format("steps: %s, step: %s, start: %s, end: %s", frameLength, tick,
                        frame, (frame + 1) % mockFrames.size());
                assertEquals(interpolated, manager.getCurrentFrame());
            }

            manager.tick();
        }
    }
}