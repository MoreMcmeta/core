/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.soir20.moremcmeta.client.animation;

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
    public void construct_FrameTimeZero_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        expectedException.expect(UnsupportedOperationException.class);
        new AnimationFrameManager<>(mockFrames, (frame) -> 0);
    }

    @Test
    public void construct_FrameTimeNegative_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        expectedException.expect(UnsupportedOperationException.class);
        new AnimationFrameManager<>(mockFrames, (frame) -> -1);
    }

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
    public void constructWithoutInterpolator_NullFrames_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<Integer>(null, (frame) -> 10);
    }

    @Test
    public void constructWithInterpolator_NullFrames_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<Integer>(null, (frame) -> 10, (steps, step, start, end) -> 10);
    }

    @Test
    public void constructWithoutInterpolator_NullTimeCalculator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), null);
    }

    @Test
    public void constructWithInterpolator_NullTimeCalculator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), null,
                (steps, step, start, end) -> 10);
    }

    @Test
    public void constructWithInterpolator_NullInterpolator_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new AnimationFrameManager<>(ImmutableList.of(1, 2, 3), (frame) -> 10, null);
    }

    @Test
    public void tickAnimation_FrameTimeZero_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frame == 1 ? 1 : 0);

        expectedException.expect(UnsupportedOperationException.class);
        frameManager.tick();
    }

    @Test
    public void tickAnimation_FrameTimeNegative_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frame == 1 ? 1 : -1);

        expectedException.expect(UnsupportedOperationException.class);
        frameManager.tick();
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
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);
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
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);
        for (int tick = 1; tick < frameLength; tick++) {
            manager.tick();
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
                (frame) -> frameLength, interpolator);

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

    @Test
    public void tickAnimationSeveral_TickNegative_IllegalArgumentException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frame == 1 ? 1 : 0);

        expectedException.expect(IllegalArgumentException.class);
        frameManager.tick(-1);
    }

    @Test
    public void tickAnimationSeveral_FrameTimeZero_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frame == 1 ? 5 : 0);

        expectedException.expect(UnsupportedOperationException.class);
        frameManager.tick(5);
    }

    @Test
    public void tickAnimationSeveral_FrameTimeNegative_UnsupportedOpException() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frame == 1 ? 1 : -1);

        expectedException.expect(UnsupportedOperationException.class);
        frameManager.tick(5);
    }

    @Test
    public void tickAnimationSeveral_TickZero_NoChange() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameTime = 10;
        AnimationFrameManager<Integer> frameManager =
                new AnimationFrameManager<>(mockFrames, (frame) -> frameTime);

        frameManager.tick(15);
        frameManager.tick(0);
        assertEquals(new Integer(2), frameManager.getCurrentFrame());
    }

    @Test
    public void tickAnimationSeveral_MaxTicksInEachFrame_FrameChanges() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 0; frame < 5; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);

        for (int nextFrame = 1; nextFrame < mockFrames.size(); nextFrame++) {
            for (int tick = 0; tick < frameLength / 5; tick++) {
                manager.tick(5);
            }
            assertEquals(new Integer(nextFrame), manager.getCurrentFrame());
        }
    }

    @Test
    public void tickAnimationSeveral_FrameTimeVaries_SecondFrameLastsCorrectTime() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames,
                (frame) -> frame * frameLength);

        for (int tick = 0; tick < (frameLength * 3) / 5; tick++) {
            manager.tick(5);
        }
        assertEquals(new Integer(3), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimationSeveral_MaxTicksInAnimation_AnimationResetsAtBeginning() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);
        for (int tick = 0; tick < frameLength * mockFrames.size() / 5; tick++) {
            manager.tick(5);
        }

        assertEquals(new Integer(1), manager.getCurrentFrame());
    }

    @Test
    public void tickAnimationSeveral_NoInterpolator_NoInterpolatedFrames() {
        ImmutableList.Builder<Integer> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 1; frame < 6; frame++) {
            mockFramesBuilder.add(frame);
        }

        ImmutableList<Integer> mockFrames = mockFramesBuilder.build();

        int frameLength = 10;
        AnimationFrameManager<Integer> manager = new AnimationFrameManager<>(mockFrames, (frame) -> frameLength);
        for (int tick = 1; tick < frameLength / 5; tick++) {
            manager.tick(5);
            assertEquals(new Integer(1), manager.getCurrentFrame());
        }
    }

    @Test
    public void tickAnimationSeveral_WithInterpolator_InterpolatedFrames() {
        ImmutableList.Builder<String> mockFramesBuilder = ImmutableList.builder();
        for (int frame = 0; frame < 5; frame++) {
            mockFramesBuilder.add(String.valueOf(frame));
        }

        ImmutableList<String> mockFrames = mockFramesBuilder.build();

        IInterpolator<String> interpolator = ((steps, step, start, end) ->
                String.format("steps: %s, step: %s, start: %s, end: %s", steps, step, start, end)
        );

        int frameLength = 100;
        AnimationFrameManager<String> manager = new AnimationFrameManager<>(mockFrames,
                (frame) -> frameLength, interpolator);

        for (int frame = 0; frame < mockFrames.size(); frame++) {
            for (int tick = 1; tick < frameLength / 5; tick++) {
                manager.tick(5);
                String interpolated = String.format("steps: %s, step: %s, start: %s, end: %s", frameLength, tick * 5,
                        frame, (frame + 1) % mockFrames.size());
                assertEquals(interpolated, manager.getCurrentFrame());
            }

            manager.tick(5);
        }
    }

}