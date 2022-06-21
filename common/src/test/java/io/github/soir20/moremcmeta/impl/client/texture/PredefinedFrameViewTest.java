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

import io.github.soir20.moremcmeta.api.client.texture.Color;
import io.github.soir20.moremcmeta.api.client.texture.FrameView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests the {@link PredefinedFrameView}.
 * @author soir20
 */
public class PredefinedFrameViewTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NulLFrame_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new PredefinedFrameView(null, 0);
    }

    @Test
    public void construct_NegativeIndex_IllegalArgException() {
        expectedException.expect(IllegalArgumentException.class);
        new PredefinedFrameView(new MockCloseableImageFrame(), -1);
    }

    @Test
    public void index_ZeroIndex_GetsIndex() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(), 0);
        assertEquals(0, (int) view.index().orElseThrow());
    }

    @Test
    public void index_PositiveIndex_GetsIndex() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(), 1);
        assertEquals(1, (int) view.index().orElseThrow());
    }

    @Test
    public void width_HasWidth_GetsWidth() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200), 1);
        assertEquals(100, view.width());
    }

    @Test
    public void height_HasHeight_GetsHeight() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200), 1);
        assertEquals(200, view.height());
    }

    @Test
    public void color_ColorOutOfBounds_PixelOutOfBoundsException() {
        PredefinedFrameView view = new PredefinedFrameView(new MockCloseableImageFrame(100, 200), 1);
        expectedException.expect(FrameView.PixelOutOfBoundsException.class);
        view.color(100, 30);
    }

    @Test
    public void color_ColorInBounds_GetsColor() {
        MockCloseableImageFrame frame = new MockCloseableImageFrame(100, 200);
        frame.mipmap(0).setColor(25, 30, new Color(100, 100, 100, 100).combine());

        PredefinedFrameView view = new PredefinedFrameView(frame, 1);
        assertEquals(new Color(100, 100, 100, 100), view.color(25, 30));
    }

}