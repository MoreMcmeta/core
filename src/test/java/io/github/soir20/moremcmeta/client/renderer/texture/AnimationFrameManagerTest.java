package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link AnimationFrameManager}.
 * @author soir20
 */
public class AnimationFrameManagerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructWithoutInterpolator_NoFrames_NullPointerException() {
        expectedException.expect(IllegalArgumentException.class);
        new AnimationFrameManager<Integer>(ImmutableList.of(), (frame) -> 10);
    }

    @Test
    public void constructWithInterpolator_NoFrames_NullPointerException() {
        expectedException.expect(IllegalArgumentException.class);
        new AnimationFrameManager<Integer>(ImmutableList.of(), (frame) -> 10, (steps, step, start, end) -> 10);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void constructWithoutInterpolator_NullFrames_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<Integer>(null, (frame) -> 10);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void constructWithInterpolator_NullFrames_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<Integer>(null, (frame) -> 10, (steps, step, start, end) -> 10);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void constructWithoutInterpolator_NullTimeCalculator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), null);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void constructWithInterpolator_NullTimeCalculator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), null,
                (steps, step, start, end) -> 10);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void constructWithInterpolator_NullInterpolator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), (frame) -> 10, null);
    }

    @Test
    public void tickAnimation_NoTicks_FirstFrame() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 0; frame < 5; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        assertEquals(new Integer(0), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimation_MaxTicksInEachFrame_FrameChanges() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 0; frame < 5; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

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
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

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
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        for (int tick = 0; tick < frameLength * mockFrames.size(); tick++) {
            manager.tick();
        }

        assertEquals(new Integer(1), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimation_NoInterpolator_NoInterpolatedFrames() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> 10);
        for (int tick = 1; tick < frameLength; tick++) {
            assertEquals(new Integer(1), manager.getCurrentFrame());
        }
    }

    @Test
    public void tickAnimation_WithInterpolator_InterpolatedFrames() {
        ImmutableList.Builder<String> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 0; frame < 5; frame++) {
            mockFramesBuilder.add(String.valueOf(frame));
        }

        ImmutableList<String> mockFrames = mockFramesBuilder.build();

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