/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2022 soir20
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

package io.github.soir20.moremcmeta.impl.client.texture;

import io.github.soir20.moremcmeta.api.client.texture.FrameGroup;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import io.github.soir20.moremcmeta.api.client.texture.PersistentFrameView;
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
public class FrameGroupImplTest {
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
        new FrameGroupImpl<>(null, PredefinedFrameView::new);
    }

    @Test
    public void constructFrameList_NullFrameInList_NullPointerException() {
        List<MockCloseableImageFrame> frames = new ArrayList<>();
        frames.add(null);

        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(frames, PredefinedFrameView::new);
    }

    @Test
    public void constructFrameList_NullViewConstructor_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new FrameGroupImpl<>(List.of(new MockCloseableImageFrame(1)), null);
    }

    @Test
    public void frame_NegativeFrameIndex_FrameIndexOutOfBoundsException() {
        FrameGroup<PredefinedFrameView> frames =
                new FrameGroupImpl<>(List.of(new MockCloseableImageFrame(1)), PredefinedFrameView::new);

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        frames.frame(-1);
    }

    @Test
    public void frame_TooLargeFrameIndex_FrameIndexOutOfBoundsException() {
        FrameGroup<PredefinedFrameView> frames =
                new FrameGroupImpl<>(List.of(new MockCloseableImageFrame(1)), PredefinedFrameView::new);

        expectedException.expect(FrameView.FrameIndexOutOfBoundsException.class);
        frames.frame(1);
    }

    @Test
    public void frame_NonZeroValidFrameIndex_FrameRetrieved() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                PredefinedFrameView::new
        );

        assertEquals(1, (int) frames.frame(1).index().orElseThrow());
    }

    @Test
    public void frame_ZeroValidFrameIndex_FrameRetrieved() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                PredefinedFrameView::new
        );

        assertEquals(0, (int) frames.frame(0).index().orElseThrow());
    }

    @Test
    public void frames_ZeroFrames_CountReturned() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(),
                PredefinedFrameView::new
        );

        assertEquals(0, frames.frames());
    }

    @Test
    public void frames_MultipleFrames_CountReturned() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                PredefinedFrameView::new
        );

        assertEquals(2, frames.frames());
    }

    @Test
    public void iterator_ZeroFrames_FramesIteratedInOrder() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(),
                PredefinedFrameView::new
        );

        List<Integer> indices = new ArrayList<>();
        frames.forEach((frame) -> indices.add(frame.index().orElseThrow()));

        assertEquals(List.of(), indices);
    }

    @Test
    public void iterator_MultipleFrames_FramesIteratedInOrder() {
        FrameGroup<PredefinedFrameView> frames = new FrameGroupImpl<>(
                List.of(new MockCloseableImageFrame(1), new MockCloseableImageFrame(1)),
                PredefinedFrameView::new
        );

        List<Integer> indices = new ArrayList<>();
        frames.forEach((frame) -> indices.add(frame.index().orElseThrow()));

        assertEquals(List.of(0, 1), indices);
    }

}