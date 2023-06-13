/*
 * MoreMcmeta is a Minecraft mod expanding texture configuration capabilities.
 * Copyright (C) 2023 soir20
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

package io.github.moremcmeta.moremcmeta.impl.client.texture;

import com.google.common.collect.ImmutableList;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameGroup;
import io.github.moremcmeta.moremcmeta.api.client.texture.FrameIndexOutOfBoundsException;
import io.github.moremcmeta.moremcmeta.api.client.texture.PersistentFrameView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the {@link FrameGroupImpl}.
 * @author soir20
 */
public final class FrameGroupImplTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructViewList_NullList_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(null);
    }

    @Test
    public void constructViewList_NullViewInList_NullPointerException() {
        List<PersistentFrameView> frames = new ArrayList<>();
        frames.add(null);

        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(frames);
    }

    @Test
    public void constructFrameList_NullList_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(null, (CloseableImageFrame frame, Integer index) -> new PredefinedFrameView(frame));
    }

    @Test
    public void constructFrameList_NullFrameInList_NullPointerException() {
        List<MockCloseableImageFrame> frames = new ArrayList<>();
        frames.add(null);

        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(frames, (frame, index) -> new PredefinedFrameView(frame));
    }

    @Test
    public void constructFrameList_NullViewConstructor_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(ImmutableList.of(new MockCloseableImageFrame(1)), null);
    }

    @Test
    public void frame_NegativeFrameIndex_FrameIndexOutOfBoundsException() {
        FrameGroup<PredefinedFrameView> frames =
                new FrameGroupImpl<>(ImmutableList.of(new MockCloseableImageFrame(1)), (frame, index) -> new PredefinedFrameView(frame));

        expectedException.expect(FrameIndexOutOfBoundsException.class);
        frames.frame(-1);
    }

    @Test
    public void frame_TooLargeFrameIndex_FrameIndexOutOfBoundsException() {
        FrameGroup<PredefinedFrameView> frames =
                new FrameGroupImpl<>(ImmutableList.of(new MockCloseableImageFrame(1)), (frame, index) -> new PredefinedFrameView(frame));

        expectedException.expect(FrameIndexOutOfBoundsException.class);
        frames.frame(1);
    }

    @Test
    public void frame_NonZeroValidFrameIndex_FrameRetrieved() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                (frame, index) -> new PredefinedFrameView(frame)
        );

        assertEquals(100, frames.frame(1).width());
    }

    @Test
    public void frame_ZeroValidFrameIndex_FrameRetrieved() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                (frame, index) -> new PredefinedFrameView(frame)
        );

        assertEquals(100, frames.frame(0).width());
    }

    @Test
    public void frames_ZeroFrames_CountReturned() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(),
                (CloseableImageFrame frame, Integer index) -> new PredefinedFrameView(frame)
        );

        assertEquals(0, frames.frames());
    }

    @Test
    public void frames_MultipleFrames_CountReturned() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                (frame, index) -> new PredefinedFrameView(frame)
        );

        assertEquals(2, frames.frames());
    }

    @Test
    public void iterator_ZeroFrames_FramesIteratedInOrder() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(),
                (CloseableImageFrame frame1, Integer index) -> new PredefinedFrameView(frame1)
        );

        List<Integer> indices = new ArrayList<>();
        frames.forEach((frame) -> indices.add(frame.width()));

        assertEquals(ImmutableList.of(), indices);
    }

    @Test
    public void iterator_MultipleFrames_FramesIteratedInOrder() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                ImmutableList.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                (frame1, index) -> new PredefinedFrameView(frame1)
        );

        List<Integer> indices = new ArrayList<>();
        frames.forEach((frame) -> indices.add(frame.width()));

        assertEquals(ImmutableList.of(100, 100), indices);
    }

}